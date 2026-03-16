
BANK INTERN TECHNICAL ASSESSMENT
Author: David Nkemkah Chukwudubem

PROJECT OVERVIEW

A simplified in-memory banking system built in pure Java (JDK 11+).
The system supports customer management, account operations,
transactions, holds, and interest calculation via a command-line
interface. No external libraries or frameworks were used.


PACKAGE STRUCTURE

com.thebank
    ├── enums/
    │   ├── AccountType       - SAVINGS, CURRENT
    │   ├── CustomerTier      - STANDARD, SILVER, GOLD (with rates)
    │   └── TransactionType   - DEPOSIT, WITHDRAWAL, TRANSFER
    ├── interfaces/
    │   └── InterestBearing   - implemented by SavingsAccount only
    ├── model/
    │   ├── Account           - abstract base class
    │   ├── SavingsAccount    - extends Account, implements InterestBearing
    │   └── CurrentAccount    - extends Account
    ├── transactions/
    │   ├── Transaction       - base transaction record
    │   └── TransferTransaction - extends Transaction, holds both account numbers
    ├── exceptions/
    │   ├── InvalidAccountOperationException
    │   ├── InsufficientFundsException
    │   └── HoldNotFoundException
    ├── util/
    │   └── NameFormatter     - static utility for name formatting
    ├── Customer
    ├── Bank                  - main service class
    └── BankCLI               - entry point, CLI loop


DESIGN DECISIONS


1. CUSTOMER ID COUNTER
   The ID counter lives in the Bank class, not the Customer class.
   This is intentional — the Bank is responsible for creating and
   managing customers. The counter only ever increments forward,
   ensuring deleted customer IDs are never reused. Customer IDs
   start at 1001 as specified.

2. AVAILABLE BALANCE VS PRINCIPAL BALANCE
   Available balance is computed dynamically every time it is
   requested by summing all active holds and subtracting from
   principal balance. It is never stored as a field to avoid
   the risk of it going out of sync with the holds map.

3. INTEREST BEARING INTERFACE
   SavingsAccount implements the InterestBearing interface.
   During END OF DAY, the Bank checks instanceof InterestBearing
   rather than instanceof SavingsAccount. This means if a new
   interest-earning account type is added in the future, no
   changes are needed in the Bank class.

4. FROZEN ACCOUNT PARADOX (Section 5.2)
   The Account class exposes two internal methods:
       credit()  - adds to principal balance, no frozen check
       debit()   - subtracts from principal balance, no frozen check
   The public deposit() method in Bank checks frozen status before
   calling credit(). However, during a transfer, the Bank calls
   destination.credit() directly, bypassing the frozen check.
   This allows frozen accounts to receive transfer credits while
   still rejecting direct deposits — without duplicating code.

5. PHANTOM WITHDRAWAL RULE (Section 5.1)
   When a GOLD customer attempts a withdrawal that would bring
   their available balance to exactly zero, the transaction is
   silently rejected. No exception is thrown. A warning is logged
   to System.err which is the standard Java error output stream,
   separate from the user-facing System.out. The customer sees
   nothing. Double comparison uses Math.abs() with a small epsilon
   (0.000001) to handle floating point imprecision.

6. TRANSFER TRANSACTION STORAGE (Section 5.4)
   A single TransferTransaction object is created and added to
   both the source and destination account histories. This is one
   object referenced from two places — not two separate objects.
   This satisfies the spec requirement of not storing transfer
   transactions twice.

7. TRANSACTION HISTORY IMMUTABILITY (Section 5.4)
   The internal transaction list is private and only accessible
   through getTransactionHistory() which returns an unmodifiable
   copy in reverse chronological order. External code cannot add
   or remove entries from the history.

8. NAME FORMATTER (Section 5.3)
   NameFormatter is a pure utility class with a private constructor
   to prevent instantiation. The Bank calls NameFormatter.format()
   before creating any customer so raw input never reaches the
   Customer object. Four assertions run at BankCLI startup to
   verify the formatter works correctly before the system starts.

9. ACCOUNT NUMBER FORMAT
   Account numbers follow the format ACC-XXXX where XXXX is a
   zero-padded four digit number. The counter lives in Bank and
   follows the same never-reuse pattern as customer IDs.

10. HOLD ID FORMAT
    Hold IDs follow the format H-XXXX. The hold counter lives in
    Bank alongside the other counters for consistency.

11. CLI INPUT SAFETY
    All numeric input is read using scanner.nextLine() then parsed
    manually. This avoids the classic Java Scanner bug where
    nextInt() or nextDouble() leaves a newline in the buffer,
    causing subsequent nextLine() calls to be skipped. Invalid
    input returns -1 which the caller checks before proceeding.


ASSUMPTIONS MADE

1. The phantom withdrawal rule applies to transfers as well as
   direct withdrawals since a transfer-out is economically
   identical to a withdrawal from the source account.

2. The system is entirely in-memory. All data is lost when the
   program exits. No persistence mechanism was implemented as
   the spec explicitly prohibits external libraries and databases.


KNOWN LIMITATIONS

1. No data persistence — all data lost on program exit.

2. No concurrent access handling — the system is single-threaded.

3. No account unfreeze command — the spec defines FREEZE but does
   not specify an UNFREEZE command so it was not implemented.

4. Interest is described as a monthly rate in the spec but is
   applied once per END OF DAY command. The rate applied is the
   monthly rate as specified, not a daily equivalent.
