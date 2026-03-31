package com.bank.exception;

public class InvalidInputException extends BankException {
    public InvalidInputException(String message) {
        super("Invalid input: " + message);
    }
}
