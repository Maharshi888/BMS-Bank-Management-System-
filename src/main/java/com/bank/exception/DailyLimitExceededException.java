package com.bank.exception;

public class DailyLimitExceededException extends BankException {
    public DailyLimitExceededException(double limit, double attempted) {
        super(String.format("Daily withdrawal limit of ₹%.2f exceeded. Attempted: ₹%.2f", limit, attempted));
    }
}
