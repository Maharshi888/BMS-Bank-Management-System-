package com.bank.model;

import java.time.LocalDate;

// CO2: Inheritance
public class CurrentAccount extends Account {

    private static final double MINIMUM_BALANCE = 5000.0;
    private static final double DAILY_WITHDRAWAL_LIMIT = 100000.0;

    public CurrentAccount(String accountHolderName, String username, String password,
                          LocalDate dateOfBirth, String aadharNumber, String panNumber,
                          String address, String mobileNumber, String educationalQualification) {
        super(accountHolderName, username, password, dateOfBirth, aadharNumber,
                panNumber, address, mobileNumber, educationalQualification, AccountType.CURRENT);
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public double getDailyWithdrawalLimit() {
        return DAILY_WITHDRAWAL_LIMIT;
    }

    @Override
    public double getInterestRate() {
        return 2.0; // 2.0% per year for current accounts
    }
}
