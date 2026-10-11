import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console banking mini-project demonstrating core object-oriented concepts.
 */
public class BankingSystem {
    private final Scanner input = new Scanner(System.in);
    private final Bank bank = new Bank();

    public static void main(String[] args) {
        new BankingSystem().run();
    }

    private void run() {
        System.out.println("====================================");
        System.out.println("       SIMPLE JAVA BANKING SYSTEM   ");
        System.out.println("====================================");

        boolean running = true;
        while (running) {
            showMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    deposit();
                    break;
                case 3:
                    withdraw();
                    break;
                case 4:
                    checkBalance();
                    break;
                case 5:
                    displayAccounts();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Please choose a number from 1 to 6.");
            }
        }

        System.out.println("Thank you for using the banking system.");
        input.close();
    }

    private void showMenu() {
        System.out.println("\n--------------- MENU ---------------");
        System.out.println("1. Create an account");
        System.out.println("2. Deposit money");
        System.out.println("3. Withdraw money");
        System.out.println("4. Check balance");
        System.out.println("5. Display all accounts");
        System.out.println("6. Exit");
    }

    private void createAccount() {
        System.out.println("\nAccount type: 1. Savings  2. Current");
        int type = readInt("Choose account type: ");
        if (type != 1 && type != 2) {
            System.out.println("Invalid account type.");
            return;
        }

        String holder = readNonEmpty("Account holder name: ");
        double openingDeposit = readAmount("Opening deposit (minimum 0): ", true);
        BankAccount account = bank.createAccount(holder, type, openingDeposit);
        System.out.println("Account created successfully.");
        System.out.println("Your account number is " + account.getAccountNumber() + ". Keep it safe.");
    }

    private void deposit() {
        BankAccount account = findAccount();
        if (account == null) return;

        double amount = readAmount("Deposit amount: ", false);
        account.deposit(amount);
        System.out.printf("Deposit successful. New balance: $%.2f%n", account.getBalance());
    }

    private void withdraw() {
        BankAccount account = findAccount();
        if (account == null) return;

        double amount = readAmount("Withdrawal amount: ", false);
        try {
            account.withdraw(amount);
            System.out.printf("Withdrawal successful. New balance: $%.2f%n", account.getBalance());
            System.out.printf("Daily withdrawal remaining: $%.2f%n", account.getDailyLimitRemaining());
        } catch (BankingException exception) {
            System.out.println("Withdrawal declined: " + exception.getMessage());
        }
    }

    private void checkBalance() {
        BankAccount account = findAccount();
        if (account == null) return;

        System.out.println("\n" + account.getAccountSummary());
    }

    private void displayAccounts() {
        List<BankAccount> accounts = bank.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts have been created yet.");
            return;
        }

        System.out.println("\n---------------- ACCOUNTS ----------------");
        for (BankAccount account : accounts) {
            System.out.println(account.getAccountSummary());
            System.out.println("-------------------------------------------");
        }
    }

    private BankAccount findAccount() {
        String accountNumber = readNonEmpty("Account number: ");
        BankAccount account = bank.findAccount(accountNumber);
        if (account == null) System.out.println("No account found with that number.");
        return account;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private double readAmount(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            try {
                double amount = Double.parseDouble(input.nextLine().trim());
                if (Double.isFinite(amount) && (allowZero ? amount >= 0 : amount > 0)) return amount;
            } catch (NumberFormatException ignored) {
                // Show the same helpful message for malformed and out-of-range values.
            }
            System.out.println(allowZero ? "Enter a valid non-negative amount." : "Enter a valid amount greater than zero.");
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = input.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This value cannot be blank.");
        }
    }
}

interface AccountOperations {
    void deposit(double amount);
    void withdraw(double amount) throws BankingException;
    double getBalance();
}

final class Bank {
    private final Map<String, BankAccount> accounts = new LinkedHashMap<>();
    private int nextAccountNumber = 1001;

    public BankAccount createAccount(String holderName, int type, double openingDeposit) {
        String accountNumber = String.valueOf(nextAccountNumber++);
        BankAccount account = (type == 1)
                ? new SavingsAccount(accountNumber, holderName, openingDeposit)
                : new CurrentAccount(accountNumber, holderName, openingDeposit);
        accounts.put(accountNumber, account);
        return account;
    }

    public BankAccount findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<BankAccount> getAccounts() {
        return new ArrayList<>(accounts.values());
    }
}

abstract class BankAccount implements AccountOperations {
    private final String accountNumber;
    private final String holderName;
    private double balance;
    private LocalDate withdrawalDate = LocalDate.now();
    private double withdrawnToday;

    protected BankAccount(String accountNumber, String holderName, double openingDeposit) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = openingDeposit;
    }

    public abstract String getAccountType();
    public abstract double getDailyWithdrawalLimit();

    @Override
    public final void deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Deposit must be greater than zero.");
        }
        balance += amount;
    }

    @Override
    public final void withdraw(double amount) throws BankingException {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new BankingException("The amount must be greater than zero.");
        }
        resetDailyUsageIfNeeded();
        if (amount > balance) throw new BankingException("Insufficient balance.");
        if (withdrawnToday + amount > getDailyWithdrawalLimit()) {
            throw new BankingException(String.format(
                    "Daily withdrawal limit exceeded. Remaining today: $%.2f.", getDailyLimitRemaining()));
        }
        balance -= amount;
        withdrawnToday += amount;
    }

    @Override
    public final double getBalance() {
        return balance;
    }

    public final String getAccountNumber() {
        return accountNumber;
    }

    public final double getDailyLimitRemaining() {
        resetDailyUsageIfNeeded();
        return Math.max(0, getDailyWithdrawalLimit() - withdrawnToday);
    }

    public final String getAccountSummary() {
        return String.format(
                "Account number: %s%nHolder: %s%nType: %s%nBalance: $%.2f%nDaily limit: $%.2f%nWithdrawn today: $%.2f%nRemaining today: $%.2f",
                accountNumber, holderName, getAccountType(), balance, getDailyWithdrawalLimit(),
                getWithdrawnToday(), getDailyLimitRemaining());
    }

    private double getWithdrawnToday() {
        resetDailyUsageIfNeeded();
        return withdrawnToday;
    }

    private void resetDailyUsageIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!withdrawalDate.equals(today)) {
            withdrawalDate = today;
            withdrawnToday = 0;
        }
    }
}

final class SavingsAccount extends BankAccount {
    private static final double DAILY_LIMIT = 20_000.00;

    SavingsAccount(String accountNumber, String holderName, double openingDeposit) {
        super(accountNumber, holderName, openingDeposit);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public double getDailyWithdrawalLimit() {
        return DAILY_LIMIT;
    }
}

final class CurrentAccount extends BankAccount {
    private static final double DAILY_LIMIT = 50_000.00;

    CurrentAccount(String accountNumber, String holderName, double openingDeposit) {
        super(accountNumber, holderName, openingDeposit);
    }

    @Override
    public String getAccountType() {
        return "Current";
    }

    @Override
    public double getDailyWithdrawalLimit() {
        return DAILY_LIMIT;
    }
}

final class BankingException extends Exception {
    BankingException(String message) {
        super(message);
    }
}
