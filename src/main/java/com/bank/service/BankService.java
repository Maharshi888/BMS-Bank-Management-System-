package com.bank.service;

import com.bank.exception.*;
import com.bank.model.*;
import com.bank.util.FileHandler;
import com.bank.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// CO5: Collections & Generics - HashMap, ArrayList
// CO2: Service layer for business logic
// CO3: Exception Handling
public class BankService {

    // CO5: HashMap for O(1) lookup
    private Map<String, Account> accountsByNumber;      // accountNumber -> Account
    private Map<String, String> usernameToAccountNumber; // username -> accountNumber

    // Daily withdrawal tracking: accountNumber -> {date, totalWithdrawn}
    private Map<String, double[]> dailyWithdrawals;

    // Loan index: loanId -> Loan (for fast admin lookup)
    private Map<String, Loan> loanIndex;

    private static BankService instance;

    private BankService() {
        dailyWithdrawals = new HashMap<>();
        loanIndex = new HashMap<>();
        loadData();
        rebuildLoanIndex();
    }

    public static BankService getInstance() {
        if (instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    // ─────────────────────────────────────────────
    // CO3: File I/O - Load from file
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void loadData() {
        Object[] data = FileHandler.loadAccounts();
        accountsByNumber = (Map<String, Account>) data[0];
        usernameToAccountNumber = (Map<String, String>) data[1];
    }

    private void saveData() {
        FileHandler.saveAccounts(accountsByNumber, usernameToAccountNumber);
    }

    /** Rebuild loanIndex from all account loans after loading data from disk. */
    private void rebuildLoanIndex() {
        loanIndex = new HashMap<>();
        for (Account acc : accountsByNumber.values()) {
            for (Loan loan : acc.getLoans()) {
                loanIndex.put(loan.getLoanId(), loan);
            }
        }
    }

    // ─────────────────────────────────────────────
    // CO1: Register / Create Account
    // ─────────────────────────────────────────────
    public Account createAccount(String fullName, String username, String password,
                                  LocalDate dob, String aadhar, String pan,
                                  String address, String mobile, String eduQual,
                                  AccountType type) throws BankException {

        // Validate inputs
        Validator.validateName(fullName);
        Validator.validatePassword(password);
        Validator.validateAadhar(aadhar);
        Validator.validatePAN(pan);
        Validator.validateMobile(mobile);

        // Check username uniqueness
        if (usernameToAccountNumber.containsKey(username.toLowerCase())) {
            throw new InvalidInputException("Username '" + username + "' is already taken.");
        }

        // Check Aadhar uniqueness
        for (Account acc : accountsByNumber.values()) {
            if (acc.getAadharNumber().equals(aadhar)) {
                throw new InvalidInputException("An account with this Aadhar number already exists.");
            }
        }

        Account account;
        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(fullName, username, password, dob, aadhar, pan,
                    address, mobile, eduQual);
        } else {
            account = new CurrentAccount(fullName, username, password, dob, aadhar, pan,
                    address, mobile, eduQual);
        }

        // Store in collections
        accountsByNumber.put(account.getAccountNumber(), account);
        usernameToAccountNumber.put(username.toLowerCase(), account.getAccountNumber());

        saveData();
        return account;
    }

    // ─────────────────────────────────────────────
    // CO1: Login
    // ─────────────────────────────────────────────
    public Account login(String identifier, String password) throws BankException {
        Account account = null;

        // Try by account number first
        if (accountsByNumber.containsKey(identifier)) {
            account = accountsByNumber.get(identifier);
        } else if (usernameToAccountNumber.containsKey(identifier.toLowerCase())) {
            String accNum = usernameToAccountNumber.get(identifier.toLowerCase());
            account = accountsByNumber.get(accNum);
        }

        if (account == null || !account.getPassword().equals(password)) {
            throw new AuthenticationException();
        }

        if (!account.isActive()) {
            throw new BankException("This account has been closed. Please contact support.");
        }

        // Apply interest on login
        applyInterestIfDue(account);
        saveData();

        return account;
    }

    // ─────────────────────────────────────────────
    // CO2: Deposit — password protected
    // ─────────────────────────────────────────────
    public void deposit(String accountNumber, double amount, String password) throws BankException {
        Validator.validateAmount(amount);
        Account account = getActiveAccount(accountNumber);
        verifyPassword(account, password);

        applyInterestIfDue(account);

        account.setBalance(account.getBalance() + amount);

        Transaction txn = new Transaction(TransactionType.CREDIT, amount,
                "Cash Deposit", accountNumber);
        txn.setBalanceAfter(account.getBalance());
        account.addTransaction(txn);

        saveData();
    }

    // ─────────────────────────────────────────────
    // CO2: Withdraw with all constraints — password protected
    // ─────────────────────────────────────────────
    public void withdraw(String accountNumber, double amount, String password) throws BankException {
        Validator.validateAmount(amount);
        Account account = getActiveAccount(accountNumber);
        verifyPassword(account, password);

        applyInterestIfDue(account);

        // Check daily withdrawal limit
        checkDailyLimit(accountNumber, amount, account.getDailyWithdrawalLimit());

        // Check minimum balance
        double newBalance = account.getBalance() - amount;
        if (newBalance < account.getMinimumBalance()) {
            throw new MinimumBalanceException(account.getMinimumBalance());
        }

        // Check sufficient balance
        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        account.setBalance(newBalance);
        updateDailyWithdrawal(accountNumber, amount);

        Transaction txn = new Transaction(TransactionType.DEBIT, amount,
                "Cash Withdrawal", accountNumber);
        txn.setBalanceAfter(account.getBalance());
        account.addTransaction(txn);

        saveData();
    }

    // ─────────────────────────────────────────────
    // CO2: Fund Transfer — password protected
    // ─────────────────────────────────────────────
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount, String password)
            throws BankException {

        Validator.validateAmount(amount);

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidInputException("Cannot transfer to the same account.");
        }

        Account sender = getActiveAccount(fromAccountNumber);
        verifyPassword(sender, password);

        Account receiver = getActiveAccount(toAccountNumber);

        applyInterestIfDue(sender);

        // Check daily limit for sender
        checkDailyLimit(fromAccountNumber, amount, sender.getDailyWithdrawalLimit());

        // Check minimum balance constraint
        double newSenderBalance = sender.getBalance() - amount;
        if (newSenderBalance < sender.getMinimumBalance()) {
            throw new MinimumBalanceException(sender.getMinimumBalance());
        }

        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException(sender.getBalance(), amount);
        }

        // Perform transfer
        sender.setBalance(newSenderBalance);
        receiver.setBalance(receiver.getBalance() + amount);

        updateDailyWithdrawal(fromAccountNumber, amount);

        // Record transactions for both accounts
        String desc = "Fund Transfer to " + toAccountNumber;
        Transaction debit = new Transaction(TransactionType.TRANSFER_OUT, amount, desc, fromAccountNumber);
        debit.setBalanceAfter(sender.getBalance());
        sender.addTransaction(debit);

        Transaction credit = new Transaction(TransactionType.TRANSFER_IN, amount,
                "Fund Transfer from " + fromAccountNumber, toAccountNumber);
        credit.setBalanceAfter(receiver.getBalance());
        receiver.addTransaction(credit);

        saveData();
    }

    // ─────────────────────────────────────────────
    // Interest Accrual
    // ─────────────────────────────────────────────

    /**
     * Credits interest to the account if it is due.
     * - Savings (3.5% p.a.): monthly credit on the last day of each month
     * - Current (2.0% p.a.): quarterly credit (March, June, September, December)
     * Sets lastInterestCreditedDate after each credit posting.
     */
    public void applyInterestIfDue(Account account) {
        LocalDate today = LocalDate.now();
        LocalDate lastCredited = account.getLastInterestCreditedDate();

        if (account instanceof SavingsAccount) {
            // Credit monthly
            LocalDate creditDue = (lastCredited == null)
                    ? today.withDayOfMonth(1)   // treat as if never credited → credit now
                    : lastCredited.plusMonths(1).withDayOfMonth(1);

            if (!today.isBefore(creditDue)) {
                double rate = account.getInterestRate();
                double interest = account.getBalance() * (rate / 100.0) / 12.0;
                if (interest >= 0.01) {
                    account.setBalance(account.getBalance() + interest);
                    String desc = "Interest Credit (" + today.getMonth().getDisplayName(
                            java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                            + " " + today.getYear() + ")";
                    Transaction txn = new Transaction(TransactionType.INTEREST_CREDIT, interest, desc,
                            account.getAccountNumber());
                    txn.setBalanceAfter(account.getBalance());
                    account.addTransaction(txn);
                    account.setLastInterestCreditedDate(today);
                }
            }
        } else if (account instanceof CurrentAccount) {
            // Credit quarterly (months 3,6,9,12)
            int month = today.getMonthValue();
            boolean isQuarterMonth = (month == 3 || month == 6 || month == 9 || month == 12);
            if (!isQuarterMonth) return;

            LocalDate thisCreditDate = today.withDayOfMonth(1);
            if (lastCredited != null && !lastCredited.isBefore(thisCreditDate)) return;

            double rate = account.getInterestRate();
            double interest = account.getBalance() * (rate / 100.0) / 4.0;
            if (interest >= 0.01) {
                account.setBalance(account.getBalance() + interest);
                String desc = "Quarterly Interest Credit (Q" + ((month / 3)) + " " + today.getYear() + ")";
                Transaction txn = new Transaction(TransactionType.INTEREST_CREDIT, interest, desc,
                        account.getAccountNumber());
                txn.setBalanceAfter(account.getBalance());
                account.addTransaction(txn);
                account.setLastInterestCreditedDate(today);
            }
        }
    }

    // ─────────────────────────────────────────────
    // Loan Operations
    // ─────────────────────────────────────────────

    public Loan applyForLoan(String accountNumber, LoanType loanType, double principal,
                             int tenureMonths) throws BankException {
        Account account = getActiveAccount(accountNumber);

        if (principal <= 0) throw new InvalidInputException("Loan principal must be positive.");
        if (tenureMonths <= 0 || tenureMonths > loanType.getMaxTenureMonths()) {
            throw new InvalidInputException("Tenure must be between 1 and "
                    + loanType.getMaxTenureMonths() + " months for " + loanType.getDisplayName() + ".");
        }

        Loan loan = new Loan(accountNumber, loanType, principal, tenureMonths);
        account.addLoan(loan);
        loanIndex.put(loan.getLoanId(), loan);
        saveData();
        return loan;
    }

    public void approveLoan(String loanId) throws BankException {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BankException("Loan " + loanId + " is not in PENDING state.");
        }

        loan.computeEMI();
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());

        // Disburse principal to account balance
        Account account = getActiveAccount(loan.getAccountNumber());
        account.setBalance(account.getBalance() + loan.getPrincipal());
        Transaction txn = new Transaction(TransactionType.LOAN_DISBURSEMENT, loan.getPrincipal(),
                "Loan Disbursement - " + loan.getLoanType().getDisplayName()
                        + " [" + loan.getLoanId() + "]", loan.getAccountNumber());
        txn.setBalanceAfter(account.getBalance());
        account.addTransaction(txn);

        saveData();
    }

    public void rejectLoan(String loanId) throws BankException {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BankException("Loan " + loanId + " is not in PENDING state.");
        }
        loan.setStatus(LoanStatus.REJECTED);
        saveData();
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loanIndex.values());
    }

    public List<Loan> getPendingLoans() {
        List<Loan> pending = new ArrayList<>();
        for (Loan loan : loanIndex.values()) {
            if (loan.getStatus() == LoanStatus.PENDING) pending.add(loan);
        }
        return pending;
    }

    private Loan getLoanById(String loanId) throws BankException {
        Loan loan = loanIndex.get(loanId);
        if (loan == null) throw new BankException("Loan not found: " + loanId);
        return loan;
    }

    // ─────────────────────────────────────────────
    // Admin: Search Account
    // ─────────────────────────────────────────────

    /** O(1) lookup by account number or username. Returns null if not found. */
    public Account searchAccount(String query) {
        if (query == null || query.isBlank()) return null;
        String q = query.trim();
        // Try by account number directly
        if (accountsByNumber.containsKey(q)) return accountsByNumber.get(q);
        // Try by username
        String accNum = usernameToAccountNumber.get(q.toLowerCase());
        return accNum != null ? accountsByNumber.get(accNum) : null;
    }

    // ─────────────────────────────────────────────
    // Delete Account
    // ─────────────────────────────────────────────
    public void closeAccount(String accountNumber) throws BankException {
        Account account = getActiveAccount(accountNumber);
        account.setActive(false);
        saveData();
    }

    // ─────────────────────────────────────────────
    // Get Account
    // ─────────────────────────────────────────────
    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account account = accountsByNumber.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    /** Return all accounts (active and inactive) — for admin view. */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accountsByNumber.values());
    }

    // CO5: Return all active accounts as a list
    public List<Account> getAllActiveAccounts() {
        List<Account> active = new ArrayList<>();
        for (Account a : accountsByNumber.values()) {
            if (a.isActive()) active.add(a);
        }
        return active;
    }

    // Export statement
    public String exportStatement(String accountNumber) throws BankException {
        Account account = getActiveAccount(accountNumber);
        String path = FileHandler.exportStatement(account);
        if (path == null) throw new BankException("Failed to generate statement.");
        return path;
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────
    private void verifyPassword(Account account, String password) throws AuthenticationException {
        if (!account.getPassword().equals(password)) {
            throw new AuthenticationException("Incorrect password. Transaction cancelled.");
        }
    }

    private Account getActiveAccount(String accountNumber) throws BankException {
        Account account = accountsByNumber.get(accountNumber);
        if (account == null) throw new AccountNotFoundException(accountNumber);
        if (!account.isActive()) throw new BankException("Account is closed.");
        return account;
    }

    private void checkDailyLimit(String accountNumber, double amount, double limit)
            throws DailyLimitExceededException {

        double[] tracking = dailyWithdrawals.get(accountNumber);
        LocalDateTime now = LocalDateTime.now();

        if (tracking != null) {
            long storedDay = (long) tracking[0];
            long today = now.toLocalDate().toEpochDay();
            if (storedDay == today) {
                double totalToday = tracking[1] + amount;
                if (totalToday > limit) {
                    throw new DailyLimitExceededException(limit, totalToday);
                }
            }
        }
    }

    private void updateDailyWithdrawal(String accountNumber, double amount) {
        LocalDateTime now = LocalDateTime.now();
        long today = now.toLocalDate().toEpochDay();
        double[] tracking = dailyWithdrawals.get(accountNumber);

        if (tracking == null || (long) tracking[0] != today) {
            dailyWithdrawals.put(accountNumber, new double[]{today, amount});
        } else {
            tracking[1] += amount;
        }
    }
}
