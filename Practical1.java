import java.util.Scanner;

public class Practical1 {
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Java Calculator ===");

        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose an option (1-6): ", 1, 6);

            if (choice == 6) {
                running = false;
                continue;
            }

            double first = readFiniteDouble("Enter the first number: ");
            double second = readFiniteDouble("Enter the second number: ");

            try {
                double result = calculate(choice, first, second);
                System.out.println("Result: " + format(result) + "\n");
            } catch (ArithmeticException exception) {
                System.out.println("Error: " + exception.getMessage() + "\n");
            }
        }

        System.out.println("Calculator closed. Goodbye!");
        INPUT.close();
    }

    private static void printMenu() {
        System.out.println("Select an operation:");
        System.out.println("1. Add (+)");
        System.out.println("2. Subtract (-)");
        System.out.println("3. Multiply (*)");
        System.out.println("4. Divide (/)");
        System.out.println("5. Power (^)");
        System.out.println("6. Exit");
    }

    private static int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String line = INPUT.nextLine().trim();

            try {
                int value = Integer.parseInt(line);
                if (value >= minimum && value <= maximum) {
                    return value;
                }
                System.out.printf("Enter a number from %d to %d.%n", minimum, maximum);
            } catch (NumberFormatException exception) {
                System.out.println("Invalid choice. Enter a whole number.");
            }
        }
    }

    private static double readFiniteDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = INPUT.nextLine().trim();

            try {
                double value = Double.parseDouble(line);
                if (Double.isFinite(value)) {
                    return value;
                }
                System.out.println("Please enter a finite number.");
            } catch (NumberFormatException exception) {
                System.out.println("Invalid number. Try again, for example: 12 or -3.5.");
            }
        }
    }

    private static double calculate(int choice, double first, double second) {
        double result;

        switch (choice) {
            case 1:
                result = first + second;
                break;
            case 2:
                result = first - second;
                break;
            case 3:
                result = first * second;
                break;
            case 4:
                if (second == 0.0) {
                    throw new ArithmeticException("Cannot divide by zero.");
                }
                result = first / second;
                break;
            case 5:
                result = Math.pow(first, second);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation.");
        }

        if (!Double.isFinite(result)) {
            throw new ArithmeticException(
                "The result is outside the supported numeric range."
            );
        }

        return result;
    }

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1.0e16) {
            return String.format("%.0f", value);
        }
        return Double.toString(value);
    }
}