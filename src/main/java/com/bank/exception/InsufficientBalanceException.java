package com.bank.exception;

public class InsufficientBalanceException extends BankException {
    public InsufficientBalanceException(double available, double requested) {
        super(String.format("Insufficient balance. Available: ₹%.2f, Requested: ₹%.2f", available, requested));
    }
}
