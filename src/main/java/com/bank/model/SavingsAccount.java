package com.bank.model;

import java.time.LocalDate;

// CO2: Inheritance
public class SavingsAccount extends Account {

    private static final double MINIMUM_BALANCE = 1000.0;
    private static final double DAILY_WITHDRAWAL_LIMIT = 25000.0;
    private static final double INTEREST_RATE = 3.5; // 3.5% per year

    public SavingsAccount(String accountHolderName, String username, String password,
                          LocalDate dateOfBirth, String aadharNumber, String panNumber,
                          String address, String mobileNumber, String educationalQualification) {
        super(accountHolderName, username, password, dateOfBirth, aadharNumber,
                panNumber, address, mobileNumber, educationalQualification, AccountType.SAVINGS);
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
        return INTEREST_RATE;
    }
}
