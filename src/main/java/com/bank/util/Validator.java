package com.bank.util;

import com.bank.exception.InvalidInputException;

public class Validator {

    public static void validateAadhar(String aadhar) throws InvalidInputException {
        if (aadhar == null || !aadhar.matches("\\d{12}")) {
            throw new InvalidInputException("Aadhar number must be exactly 12 digits.");
        }
    }

    public static void validatePAN(String pan) throws InvalidInputException {
        if (pan == null || !pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
            throw new InvalidInputException("PAN number must follow format: ABCDE1234F");
        }
    }

    public static void validateMobile(String mobile) throws InvalidInputException {
        if (mobile == null || !mobile.matches("[6-9]\\d{9}")) {
            throw new InvalidInputException("Mobile number must be a valid 10-digit Indian number.");
        }
    }

    public static void validatePassword(String password) throws InvalidInputException {
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters long.");
        }
    }

    public static void validateAmount(double amount) throws InvalidInputException {
        if (amount <= 0) {
            throw new InvalidInputException("Amount must be greater than zero.");
        }
    }

    public static void validateName(String name) throws InvalidInputException {
        if (name == null || name.trim().length() < 2) {
            throw new InvalidInputException("Name must be at least 2 characters long.");
        }
    }
}
