package com.bank.exception;

public class AuthenticationException extends BankException {
    public AuthenticationException() {
        super("Invalid username/account number or password.");
    }
    public AuthenticationException(String message) {
        super(message);
    }
}
