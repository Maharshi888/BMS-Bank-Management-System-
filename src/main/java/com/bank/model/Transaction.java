package com.bank.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {

    private String transactionId;
    private TransactionType type;
    private double amount;
    private String description;
    private String accountNumber;
    private LocalDateTime timestamp;
    private double balanceAfter;

    public Transaction(TransactionType type, double amount, String description, String accountNumber) {
        this.transactionId = "TXN" + System.currentTimeMillis();
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.accountNumber = accountNumber;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getAccountNumber() { return accountNumber; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getBalanceAfter() { return balanceAfter; }

    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | ₹%.2f | Balance: ₹%.2f",
                getFormattedTimestamp(), transactionId, type, amount, balanceAfter);
    }
}
