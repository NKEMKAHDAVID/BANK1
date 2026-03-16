package com.thebank.model;

import com.thebank.enums.AccountType;
import com.thebank.interfaces.InterestBearing;

public class SavingsAccount extends Account implements InterestBearing {

    public SavingsAccount(String accountNumber, int customerId, double openingBalance) {

        super(accountNumber, AccountType.SAVINGS, customerId, openingBalance);
    }


    @Override
    public void applyInterest(double monthlyInterestRate) {
        double interest = getPrincipalBalance() * monthlyInterestRate;
        setPrincipalBalance(getPrincipalBalance() + interest);
    }
}