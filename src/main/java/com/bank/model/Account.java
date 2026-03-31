package com.bank.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// CO2: Class Design & Encapsulation
public abstract class Account implements Serializable {

    private String accountNumber;
    private String accountHolderName;
    private double balance;
    private String password;
    private String username;
    private LocalDate dateOfBirth;
    private String aadharNumber;
    private String panNumber;
    private String address;
    private String mobileNumber;
    private String educationalQualification;
    private AccountType accountType;
    private LocalDateTime createdAt;
    private boolean active;

    // CO5: Collections usage
    private List<Transaction> transactionHistory;
    private List<Loan> loans;

    // Interest tracking: date up to which interest was last credited
    private LocalDate lastInterestCreditedDate;

    public Account(String accountHolderName, String username, String password,
                   LocalDate dateOfBirth, String aadharNumber, String panNumber,
                   String address, String mobileNumber, String educationalQualification,
                   AccountType accountType) {
        this.accountNumber = AccountNumberGenerator.generate();
        this.accountHolderName = accountHolderName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.aadharNumber = aadharNumber;
        this.panNumber = panNumber;
        this.address = address;
        this.mobileNumber = mobileNumber;
        this.educationalQualification = educationalQualification;
        this.accountType = accountType;
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.transactionHistory = new ArrayList<>();
        this.loans = new ArrayList<>();
        this.lastInterestCreditedDate = null; // null → never credited yet

        // Set initial minimum balance
        this.balance = getMinimumBalance();
        addTransaction(new Transaction(TransactionType.CREDIT, getMinimumBalance(),
                "Initial deposit (minimum balance)", this.accountNumber));
    }

    // Abstract methods - CO2: Inheritance
    public abstract double getMinimumBalance();
    public abstract double getDailyWithdrawalLimit();
    public abstract double getInterestRate(); // per annum, e.g. 3.5 means 3.5%

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance() { return balance; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAadharNumber() { return aadharNumber; }
    public String getPanNumber() { return panNumber; }
    public String getAddress() { return address; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEducationalQualification() { return educationalQualification; }
    public AccountType getAccountType() { return accountType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public List<Transaction> getTransactionHistory() { return new ArrayList<>(transactionHistory); }
    public List<Loan> getLoans() { return loans != null ? new ArrayList<>(loans) : new ArrayList<>(); }
    public LocalDate getLastInterestCreditedDate() { return lastInterestCreditedDate; }

    // Setters
    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { this.active = active; }
    public void setPassword(String password) { this.password = password; }
    public void setLastInterestCreditedDate(LocalDate date) { this.lastInterestCreditedDate = date; }

    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }

    public void addLoan(Loan loan) {
        if (this.loans == null) this.loans = new ArrayList<>();
        this.loans.add(loan);
    }
}
