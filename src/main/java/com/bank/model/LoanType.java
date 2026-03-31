package com.bank.model;

/**
 * Enum representing supported loan types with associated interest rates and max tenures.
 */
public enum LoanType {
    HOME("Home Loan", 8.5, 240),
    PERSONAL("Personal Loan", 14.0, 60),
    BUSINESS("Business Loan", 11.5, 120),
    EDUCATION("Education Loan", 9.0, 96),
    CAR("Car Loan", 10.0, 84);

    private final String displayName;
    private final double interestRatePA; // per annum
    private final int maxTenureMonths;

    LoanType(String displayName, double interestRatePA, int maxTenureMonths) {
        this.displayName = displayName;
        this.interestRatePA = interestRatePA;
        this.maxTenureMonths = maxTenureMonths;
    }

    public String getDisplayName() { return displayName; }
    public double getInterestRatePA() { return interestRatePA; }
    public int getMaxTenureMonths() { return maxTenureMonths; }

    @Override
    public String toString() { return displayName; }
}
