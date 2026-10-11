import java.util.Scanner;

class BankAccount1 {
    private double balance;

    BankAccount1(double openingBalance) {
        if (openingBalance < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative.");
        }
        balance = openingBalance;
    }

    double getBalance() {
        return balance;
    }

    void deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Deposit must be a positive amount.");
        }

        balance += amount;
        System.out.printf("$%.2f deposited successfully.%n", amount);
    }

    void withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal must be a positive amount.");
        }
        if (amount > balance) {
            throw new ArithmeticException("Insufficient funds.");
        }

        balance -= amount;
        System.out.printf("$%.2f withdrawn successfully.%n", amount);
    }
}

public class Practical4 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BankAccount1 account = new BankAccount1(1000.00);
        boolean running = true;

        System.out.println("Welcome to the ATM!");

        while (running) {
            System.out.println("\n1. Check balance");
            System.out.println("2. Deposit money");
            System.out.println("3. Withdraw money");
            System.out.println("4. Exit");

            try {
                int choice = readChoice(scanner);

                switch (choice) {
                    case 1:
                        System.out.printf("Current balance: $%.2f%n",
                                account.getBalance());
                        break;
                    case 2:
                        account.deposit(readAmount(scanner, "Enter deposit amount: "));
                        break;
                    case 3:
                        account.withdraw(readAmount(scanner, "Enter withdrawal amount: "));
                        break;
                    case 4:
                        running = false;
                        System.out.println("Thank you for using the ATM.");
                        break;
                    default:
                        // readChoice validates the range before returning.
                        throw new IllegalArgumentException("Invalid menu option.");
                }
            } catch (ArithmeticException e) {
                System.out.println("Transaction error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Input error: " + e.getMessage());
            } finally {
                System.out.println("Transaction attempt complete.");
            }
        }

        scanner.close();
    }

    private static int readChoice(Scanner scanner) {
        System.out.print("Choose an option (1-4): ");
        String input = scanner.nextLine().trim();

        final int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole number from 1 to 4.");
        }

        if (choice < 1 || choice > 4) {
            throw new IllegalArgumentException("Choose an option from 1 to 4.");
        }

        return choice;
    }

    private static double readAmount(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid numeric amount.");
        }
    }
}