package com.thebank.transactions;

import com.thebank.enums.TransactionType;

import java.sql.Date;
import java.time.format.DateTimeFormatter;


public class TransferTransaction extends Transaction {

    private final String sourceAccountNumber;
    private final String destinationAccountNumber;

    public TransferTransaction(double amount, String sourceAccountNumber,
                               String destinationAccountNumber) {
        super(amount, TransactionType.TRANSFER);
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
    }


    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    @Override
    public String toString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh-mm-ss");
        return String.format("[%s] %s %.2f || From: %s - To: %s",
                getTimestamp().format(formatter),
                getTransactionType(),
                getAmount(),
                sourceAccountNumber,
                destinationAccountNumber
                );
    }
}