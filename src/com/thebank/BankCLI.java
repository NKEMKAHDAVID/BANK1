package com.thebank;

import com.thebank.model.Account;
import com.thebank.enums.AccountType;
import com.thebank.enums.CustomerTier;
import com.thebank.exceptions.HoldNotFoundException;
import com.thebank.exceptions.InsufficientFundsException;
import com.thebank.exceptions.InvalidAccountOperationException;
import com.thebank.util.NameFormatter;
import java.util.Scanner;

public class BankCLI {

    private static boolean runNameFormatterTests() {
        String t1 = NameFormatter.format("john doe");
        if (!t1.equals("John Doe")) {
            System.out.println("TEST FAILED: expected 'John Doe' got '" + t1 + "'");
            return false;
        }

        String t2 = NameFormatter.format("ahmad bin hassan");
        if (!t2.equals("Ahmad bin Hassan")) {
            System.out.println("TEST FAILED: expected 'Ahmad bin Hassan' got '" + t2 + "'");
            return false;
        }

        String t3 = NameFormatter.format("bin laden");
        if (!t3.equals("Bin Laden")) {
            System.out.println("TEST FAILED: expected 'Bin Laden' got '" + t3 + "'");
            return false;
        }

        String t4 = NameFormatter.format("  john   van   dam  ");
        if (!t4.equals("John van Dam")) {
            System.out.println("TEST FAILED: expected 'John van Dam' got '" + t4 + "'");
            return false;
        }

        System.out.println("All NameFormatter tests passed.");
        return true;
    }

    private static final Bank bank = new Bank();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (!runNameFormatterTests()) {
            System.out.println("Startup tests failed. Exiting.");
            return;
        }

        System.out.println("Welcome to The Bank");
        System.out.println("Type HELP to see available commands");
        while (true) {
            System.out.print("\nEnter command: ");
            String command = scanner.nextLine().trim().toUpperCase();

            switch (command) {
                case "ADD CUSTOMER":
                    addCustomer();
                    break;
                case "OPEN ACCOUNT":
                    openAccount();
                    break;
                case "DEPOSIT":
                    deposit();
                    break;
                case "WITHDRAW":
                    withdraw();
                    break;
                case "TRANSFER":
                    transfer();
                    break;
                case "HOLD":
                    placeHold();
                    break;
                case "RELEASE HOLD":
                    releaseHold();
                    break;
                case "STATEMENT":
                    printStatement();
                    break;
                case "END OF DAY":
                    endOfDay();
                    break;
                case "FREEZE":
                    freeze();
                    break;
                case "LIST CUSTOMERS":
                    bank.listCustomers();
                    break;
                case "HELP":
                    printHelp();
                    break;
                case "EXIT":
                    System.out.println("Goodbye.");
                    return;
                default:
                    System.out.println("Unknown command. Type HELP to see available commands.");
            }
        }
    }

    private static void addCustomer() {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine().trim();

        CustomerTier tier = promptTier();
        if (tier == null) return;

        Customer customer = bank.addCustomer(name, tier);
        System.out.println("Customer added successfully.");
        System.out.println(customer.toString());
    }

    private static void openAccount() {
        System.out.print("Enter customer ID: ");
        int customerId = readInt();

        AccountType type = promptAccountType();
        if (type == null) return;

        System.out.print("Enter opening balance: ");
        double balance = readDouble();
        if (balance == -1) return;

        try {
            Account account = bank.openAccount(customerId, type, balance);
            System.out.println("Account opened successfully.");
            System.out.println("Account Number: " + account.getAccountNumber());
        } catch (InvalidAccountOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deposit() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter deposit amount: ");
        double amount = readDouble();
        if (amount == -1) return;

        try {
            bank.deposit(accountNumber, amount);
        } catch (InvalidAccountOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private static void withdraw() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter withdrawal amount: ");
        double amount = readDouble();
        if (amount == -1) return;

        try {
            bank.withdraw(accountNumber, amount);
            System.out.println("Withdrawal successful.");
        } catch (InvalidAccountOperationException | InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void transfer() {
        System.out.print("Enter source account number: ");
        String source = scanner.nextLine().trim().toUpperCase();

        System.out.println("Enter destination account number: ");
        String destination = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter transfer amount: ");
        double amount = readDouble();
        if (amount == -1) return;

        try {
            bank.transfer(source, destination, amount);
            System.out.println("Transfer successful.");
        } catch (InvalidAccountOperationException | InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void placeHold() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter hold amount: ");
        double amount = readDouble();
        if (amount == -1) return;

        try {
            String holdId = bank.placeHold(accountNumber, amount);
            System.out.println("Hold placed successfully. Hold ID: " + holdId);
        } catch (InvalidAccountOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void releaseHold() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter hold ID: ");
        String holdId = scanner.nextLine().trim().toUpperCase();

        try {
            bank.releaseHold(accountNumber, holdId);
            System.out.println("Hold " + holdId + " released successfully.");
        } catch (InvalidAccountOperationException | HoldNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printStatement() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        try {
            bank.printStatement(accountNumber);
        } catch (InvalidAccountOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void endOfDay() {
        bank.applyEndOfDay();
        System.out.println("End of day interest applied to all savings accounts.");
    }

    private static void freeze() {
        System.out.print("Enter account number to freeze: ");
        String accountNumber = scanner.nextLine().trim().toUpperCase();

        try {
            bank.freezeAccount(accountNumber);
            System.out.println("Account " + accountNumber + " frozen successfully.");
        } catch (InvalidAccountOperationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("========================================");
        System.out.println("AVAILABLE COMMANDS:");
        System.out.println("  ADD CUSTOMER   - Add a new customer");
        System.out.println("  OPEN ACCOUNT   - Open a new account");
        System.out.println("  DEPOSIT        - Deposit funds");
        System.out.println("  WITHDRAW       - Withdraw funds");
        System.out.println("  TRANSFER       - Transfer between accounts");
        System.out.println("  HOLD           - Place a hold on an account");
        System.out.println("  RELEASE HOLD   - Release a hold");
        System.out.println("  STATEMENT      - Print account statement");
        System.out.println("  END OF DAY     - Apply interest to savings");
        System.out.println("  FREEZE         - Freeze an account");
        System.out.println("  LIST CUSTOMERS - List all customers");
        System.out.println("  EXIT           - Exit the program");
        System.out.println("========================================");
    }



    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
            return -1;
        }
    }

    private static double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
            return -1;
        }
    }

    private static CustomerTier promptTier() {
        System.out.println("Select tier: 1. STANDARD  2. SILVER  3. GOLD");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": return CustomerTier.STANDARD;
            case "2": return CustomerTier.SILVER;
            case "3": return CustomerTier.GOLD;
            default:
                System.out.println("Invalid tier selection.");
                return null;
        }
    }

    private static AccountType promptAccountType() {
        System.out.println("Select an account type: 1.SAVINGS  2. CURRENT");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": return AccountType.SAVINGS;
            case "2": return AccountType.CURRENT;
            default:
                System.out.println("Invalid selection.");
                return null;
        }
    }
}