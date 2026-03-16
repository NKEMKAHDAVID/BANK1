package com.thebank.model;

import com.thebank.enums.AccountType;

public class CurrentAccount extends Account {

    public CurrentAccount(String accountNumber, int customerId,
                          double openingBalance) {
        super(accountNumber, AccountType.CURRENT, customerId, openingBalance);
    }
}