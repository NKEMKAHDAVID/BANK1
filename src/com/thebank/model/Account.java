package com.thebank.model;

import com.thebank.enums.AccountType;
import com.thebank.transactions.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Account {

    private final String accountNumber;
    private final AccountType accountType;
    private final int customerId;
    private double principalBalance;
    private boolean frozen;


    private final Map<String, Double> holds;


    private final List<Transaction> transactionHistory;


    public Account(String accountNumber, AccountType accountType, int customerId, double openingBalance) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.customerId = customerId;
        this.principalBalance = openingBalance;
        this.frozen = false;
        this.holds = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }



    public double getPrincipalBalance() {
        return principalBalance;
    }

    public double getAvailableBalance() {
        double totalHolds = 0;
        for (double holdAmount : holds.values()) {
            totalHolds += holdAmount;
        }
        return principalBalance - totalHolds;
    }


    protected void setPrincipalBalance(double amount) {
        this.principalBalance = amount;
    }


    public void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    public List<Transaction> getTransactionHistory() {
        List<Transaction> reversed = new ArrayList<>();
        for (int i = transactionHistory.size() - 1; i >= 0; i--) {
            reversed.add(transactionHistory.get(i));
        }
        return Collections.unmodifiableList(reversed);
    }


    public void addHold(String holdId, double amount) {
        holds.put(holdId, amount);
    }

    public void releaseHold(String holdId) {
        holds.remove(holdId);
    }

    public boolean holdExists(String holdId) {
        return holds.containsKey(holdId);
    }

    public Map<String, Double> getHolds() {
        return Collections.unmodifiableMap(holds);
    }

    public void freeze() {
        this.frozen = true;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public int getCustomerId() {
        return customerId;
    }
    public void credit(double amount){
        this.principalBalance += amount;
    }
    public void debit(double amount){
        this.principalBalance -= amount;
    }
}