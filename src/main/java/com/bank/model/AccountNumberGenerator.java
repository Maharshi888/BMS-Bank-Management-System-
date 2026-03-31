package com.bank.model;

import java.util.Random;

public class AccountNumberGenerator {

    private static final String PREFIX = "BMS";

    public static String generate() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return PREFIX + number;
    }
}
