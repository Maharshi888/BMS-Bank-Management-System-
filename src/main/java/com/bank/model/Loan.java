package com.bank.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a loan application attached to a bank account.
 * EMI = P * r * (1+r)^n / ((1+r)^n - 1)   where r = annualRate / 12 / 100
 */
public class Loan implements Serializable {

    private final String loanId;
    private final String accountNumber;
    private final LoanType loanType;
    private final double principal;
    private final double interestRatePA;
    private final int tenureMonths;
    private LoanStatus status;
    private final LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private double emiAmount;          // computed on approval
    private int remainingInstallments;
    private LocalDate nextDueDate;

    public Loan(String accountNumber, LoanType loanType, double principal, int tenureMonths) {
        this.loanId = "LN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.loanType = loanType;
        this.principal = principal;
        this.interestRatePA = loanType.getInterestRatePA();
        this.tenureMonths = tenureMonths;
        this.status = LoanStatus.PENDING;
        this.appliedAt = LocalDateTime.now();
    }

    /** Calculate and fix the EMI — called when the loan is approved. */
    public void computeEMI() {
        double r = interestRatePA / 12.0 / 100.0;
        if (r == 0) {
            emiAmount = principal / tenureMonths;
        } else {
            double factor = Math.pow(1 + r, tenureMonths);
            emiAmount = principal * r * factor / (factor - 1);
        }
        remainingInstallments = tenureMonths;
        nextDueDate = LocalDate.now().plusMonths(1);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getLoanId() { return loanId; }
    public String getAccountNumber() { return accountNumber; }
    public LoanType getLoanType() { return loanType; }
    public double getPrincipal() { return principal; }
    public double getInterestRatePA() { return interestRatePA; }
    public int getTenureMonths() { return tenureMonths; }
    public LoanStatus getStatus() { return status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public double getEmiAmount() { return emiAmount; }
    public int getRemainingInstallments() { return remainingInstallments; }
    public LocalDate getNextDueDate() { return nextDueDate; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setStatus(LoanStatus status) { this.status = status; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    // ── Display helpers (for TableView PropertyValueFactory) ──────────────────

    public String getLoanTypeDisplay() { return loanType.getDisplayName(); }
    public String getStatusDisplay() { return status.getDisplayName(); }
    public String getAppliedAtDisplay() {
        return appliedAt != null ? appliedAt.toLocalDate().toString() : "-";
    }
    public String getApprovedAtDisplay() {
        return approvedAt != null ? approvedAt.toLocalDate().toString() : "-";
    }
    public String getNextDueDateDisplay() {
        return nextDueDate != null ? nextDueDate.toString() : "-";
    }

    /**
     * Static helper: compute EMI without creating a Loan (for real-time preview in UI).
     */
    public static double computeEMI(double principal, double annualRatePercent, int tenureMonths) {
        double r = annualRatePercent / 12.0 / 100.0;
        if (r == 0) return principal / tenureMonths;
        double factor = Math.pow(1 + r, tenureMonths);
        return principal * r * factor / (factor - 1);
    }
}
