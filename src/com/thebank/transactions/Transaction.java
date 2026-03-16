package com.thebank.transactions;

import com.thebank.enums.TransactionType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final TransactionType transactionType;
    private final double amount;
    private final LocalDateTime timestamp;


    public Transaction(double amount, TransactionType transactionType){
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount(){
        return amount;
    }
    public TransactionType getTransactionType(){
        return transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh-mm-ss");
        return String.format("[%s] %s - Amount: %.2f",
                timestamp.format(formatter),
                transactionType.getDisplayName(),
                amount);
    }

}
