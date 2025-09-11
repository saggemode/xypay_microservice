package com.xypay.account.enums;

public enum AccountType {
    SAVINGS("Savings Account", 0.05, 1000.0, 50000.0),
    CHECKING("Checking Account", 0.0, 500.0, 100000.0),
    BUSINESS("Business Account", 0.0, 5000.0, 500000.0),
    CORPORATE("Corporate Account", 0.0, 10000.0, 1000000.0),
    FIXED_DEPOSIT("Fixed Deposit", 0.12, 10000.0, 10000000.0),
    CURRENT("Current Account", 0.0, 1000.0, 200000.0),
    STUDENT("Student Account", 0.02, 100.0, 10000.0),
    SENIOR_CITIZEN("Senior Citizen Account", 0.08, 500.0, 100000.0),
    PREMIUM("Premium Account", 0.06, 10000.0, 1000000.0),
    BASIC("Basic Account", 0.0, 100.0, 5000.0);

    private final String description;
    private final double interestRate;
    private final double minimumBalance;
    private final double maximumBalance;

    AccountType(String description, double interestRate, double minimumBalance, double maximumBalance) {
        this.description = description;
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
        this.maximumBalance = maximumBalance;
    }

    public String getDescription() {
        return description;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getMaximumBalance() {
        return maximumBalance;
    }

    public boolean isInterestBearing() {
        return interestRate > 0;
    }

    public boolean requiresMinimumBalance() {
        return minimumBalance > 0;
    }
}
