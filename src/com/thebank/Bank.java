package com.thebank;

import com.thebank.enums.AccountType;
import com.thebank.enums.CustomerTier;
import com.thebank.enums.TransactionType;
import com.thebank.exceptions.HoldNotFoundException;
import com.thebank.exceptions.InsufficientFundsException;
import com.thebank.exceptions.InvalidAccountOperationException;
import com.thebank.interfaces.InterestBearing;
import com.thebank.model.Account;
import com.thebank.model.CurrentAccount;
import com.thebank.model.SavingsAccount;
import com.thebank.transactions.Transaction;
import com.thebank.transactions.TransferTransaction;
import com.thebank.util.NameFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bank{

    private int nextCustomerId = 1001;
    private int nextAccountNumber = 1;


    private final Map<Integer, Customer> customers = new HashMap<>();
    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<Integer, List<Account>> customerAccounts = new HashMap<>();


    public Customer addCustomer(String name, CustomerTier tier){
        String cleanName = NameFormatter.format(name);
        Customer customer = new Customer(nextCustomerId, cleanName, tier);
        customers.put(nextCustomerId , customer);
        customerAccounts.put(nextCustomerId, new ArrayList<>());
        nextCustomerId++;
        return customer;
    }
    public Customer findCustomer(int customerId) throws InvalidAccountOperationException {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new InvalidAccountOperationException("Customer " + customerId + " does not exist.");
        }
        return customer;
    }
    public void deleteCustomer(int customerId) throws InvalidAccountOperationException {
        Customer customer = findCustomer(customerId);
        List<Account> owned = customerAccounts.get(customerId);
        if (!owned.isEmpty()) {
            throw new InvalidAccountOperationException("Cannot delete customer " + customerId + ", they still have open accounts.");
        }
        customers.remove(customerId);
        customerAccounts.remove(customerId);
    }
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }


    public Account openAccount(int customerId, AccountType type, double openingBalance ) throws InvalidAccountOperationException{
        if(openingBalance < 0){
            throw new InvalidAccountOperationException("Opening Balance can Not be negative");
        }
        Customer customer = findCustomer(customerId);
        List<Account> owned = customerAccounts.get(customerId);
        for (Account account : owned) {
            if (account.getAccountType() == type) {
                throw new InvalidAccountOperationException("Customer already has a " + type.getDisplayName() + ".");
            }
        }
        String accountNumber = String.format("ACC-%04d", nextAccountNumber);
        nextAccountNumber++;
        Account account;
        if (type == AccountType.SAVINGS) {
            account = new SavingsAccount(accountNumber, customerId, openingBalance);
        } else {
            account = new CurrentAccount(accountNumber, customerId, openingBalance);
        }

        accounts.put(accountNumber, account);
        owned.add(account);

        return account;
    }


    public Account findAccount(String accountNumber) throws InvalidAccountOperationException{
        Account account = accounts.get(accountNumber);
        if(account == null){
            throw new InvalidAccountOperationException("Account " + accountNumber + " does not exist. ");
        }
        return account;
    }

    public void freezeAccount(String accountNumber) throws InvalidAccountOperationException{
        Account account = findAccount(accountNumber);
        account.freeze();
    }

    public void deposit (String accountNumber, double amount) throws InvalidAccountOperationException {
    Account account = findAccount(accountNumber);

    if(account.isFrozen()){
        throw new InvalidAccountOperationException("Account " + accountNumber + " is frozen");
    }
    if(amount <= 0){
        throw new InvalidAccountOperationException("THe amount can not be zero or below");
    }
    account.credit(amount);
    account.addTransaction(new Transaction(amount, TransactionType.DEPOSIT));
    System.out.println("Deposit of " + amount + " successful");
    }

    public void withdraw(String accountNumber, double amount) throws InvalidAccountOperationException, InsufficientFundsException{
        Account account =  findAccount(accountNumber);
        if(account.isFrozen()){
            throw new InvalidAccountOperationException("Account " + accountNumber + " is frozen");
        }
        if(amount > account.getAvailableBalance()){
            throw new InsufficientFundsException("Error:  Insufficient funds ");
        }
       if (amount <= 0) {
           throw new InvalidAccountOperationException("Withdrawal amount must be greater than zero.");
       }

       Customer customer = findCustomer(account.getCustomerId());
        double balanceAfter = account.getAvailableBalance() - amount ;
        if (customer.getTier() == CustomerTier.GOLD && balanceAfter == 0){

                String warning = "[WARNING] Phantom withdrawal rule triggered. " +
                        "Account: " + accountNumber +
                        " | Customer: " + customer.getName() +
                        " | Amount: " + amount;

                System.err.println(warning);
                return;
        }

        account.debit(amount);
        account.addTransaction(new Transaction(amount, TransactionType.WITHDRAWAL));
    }

    public void transfer(String sourceNumber, String destinationNumber, double amount) throws InvalidAccountOperationException, InsufficientFundsException {

        Account source = findAccount(sourceNumber);
        Account destination = findAccount(destinationNumber);
        Customer sourceCustomer = findCustomer(source.getCustomerId());

        if (source.isFrozen()) {
            throw new InvalidAccountOperationException("Source account " + sourceNumber + " is frozen.");
        }
        if (amount <= 0) {
            throw new InvalidAccountOperationException("Transfer amount must be greater than zero.");
        }

        double available = source.getAvailableBalance();
        if (amount > available) {
            throw new InsufficientFundsException("Insufficient funds in source account. " + "Available: " + available);
        }
        double balanceAfter = available - amount;
        if (sourceCustomer.getTier() == CustomerTier.GOLD && balanceAfter == 0) {
            String warning = "[WARNING] Phantom withdrawal rule triggered " +
                    "on transfer. Account: " + sourceNumber +
                    " | Amount: " + amount;
            System.err.println(warning);
            return;
        }
        source.debit(amount);
        destination.credit(amount);

        TransferTransaction record = new TransferTransaction(amount, sourceNumber, destinationNumber);
        source.addTransaction(record);
        destination.addTransaction(record);
    }

    private int nextHoldId = 1;

    public String placeHold(String accountNumber, double amount) throws InvalidAccountOperationException {

        Account account = findAccount(accountNumber);
        if (amount <= 0) {
            throw new InvalidAccountOperationException("Hold amount must be greater than zero.");
        }

        String holdId = String.format("H-%04d", nextHoldId);
        nextHoldId++;
        account.addHold(holdId, amount);
        return holdId;
    }

    public void releaseHold(String accountNumber, String holdId) throws InvalidAccountOperationException, HoldNotFoundException {

        Account account = findAccount(accountNumber);

        if (!account.holdExists(holdId)) {
            throw new HoldNotFoundException("Hold " + holdId + " does not exist on account " + accountNumber);
        }

        account.releaseHold(holdId);
    }

    public void applyEndOfDay() {
        for (Account account : accounts.values()) {
            if (account instanceof InterestBearing) {
                InterestBearing ib = (InterestBearing) account;
                Customer customer = customers.get(account.getCustomerId());
                double rate = customer.getTier().getMonthlyInterestRate();
                ib.applyInterest(rate);
            }
        }
    }

    public void printStatement(String accountNumber) throws InvalidAccountOperationException {

        Account account = findAccount(accountNumber);
        Customer customer = findCustomer(account.getCustomerId());

        System.out.println("========================================");
        System.out.println("ACCOUNT STATEMENT");
        System.out.println("Account : " + account.getAccountNumber());
        System.out.println("Owner   : " + customer.getName());
        System.out.println("Type    : " + account.getAccountType().getDisplayName());
        System.out.println("Principal Balance  : " + String.format("%.2f", account.getPrincipalBalance()));
        System.out.println("Available Balance  : " + String.format("%.2f", account.getAvailableBalance()));
        System.out.println("Status  : " + (account.isFrozen() ? "FROZEN" : "ACTIVE"));
        System.out.println("----------------------------------------");
        System.out.println("TRANSACTIONS (most recent first):");

        List<Transaction> history = account.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions");
        } else {
            for (Transaction t : history) {
                System.out.println(t.toString());
            }
        }
    }

    public void listCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        for (Customer customer : customers.values()) {
            System.out.println("----------------------------------------");
            System.out.println(customer.toString());
            List<Account> owned = customerAccounts.get(customer.getId());
            if (owned.isEmpty()) {
                System.out.println("  No accounts.");
            } else {
                for (Account account : owned) {
                    System.out.println("  " + account.getAccountNumber() +
                            " | " + account.getAccountType().getDisplayName() +
                            " | Principal balance: " +
                            String.format("%.2f",
                                    account.getPrincipalBalance()) +
                            " | Available balance : " +
                            String.format("%.2f",
                                    account.getAvailableBalance()) +
                            " | ");
                }
            }
        }
        System.out.println("----------------------------------------");
    }

}
