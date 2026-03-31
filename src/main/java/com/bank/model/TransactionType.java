package com.bank.model;

public enum TransactionType {
    CREDIT("Credit"),
    DEBIT("Debit"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    LOAN_DISBURSEMENT("Loan Disbursement"),
    INTEREST_CREDIT("Interest Credit");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
