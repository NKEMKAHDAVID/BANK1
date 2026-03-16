package com.thebank.enums;

public enum CustomerTier {
    STANDARD(0.005),
    SILVER(0.010),
    GOLD(0.015);

    private final double monthlyInterestRate;


    CustomerTier(double monthlyInterestRate) {
        this.monthlyInterestRate = monthlyInterestRate;
    }

    public double getMonthlyInterestRate() {
        return monthlyInterestRate;
    }
}
