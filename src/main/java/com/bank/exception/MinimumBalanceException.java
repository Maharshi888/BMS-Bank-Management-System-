package com.bank.exception;

public class MinimumBalanceException extends BankException {
    public MinimumBalanceException(double minimum) {
        super(String.format("Transaction would violate minimum balance requirement of ₹%.2f", minimum));
    }
}
