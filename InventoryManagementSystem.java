import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Console inventory mini-project with purchase, shipping, stock, and reports. */
public class InventoryManagementSystem {
    private final Scanner input = new Scanner(System.in);
    private final Inventory inventory = new Inventory();

    public static void main(String[] args) {
        new InventoryManagementSystem().run();
    }

    private void run() {
        boolean running = true;
        while (running) {
            showMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1: listProducts(); break;
                case 2: displayProduct(); break;
                case 3: addProduct(); break;
                case 4: purchase(); break;
                case 5: ship(); break;
                case 6: showStockBalance(); break;
                case 7: calculateProfitLoss(); break;
                case 8: purchaseReport(); break;
                case 9: running = false; break;
                default: System.out.println("Please choose a number from 1 to 9.");
            }
        }
        System.out.println("Inventory system closed.");
        input.close();
    }

    private void showMenu() {
        System.out.println("\n========== INVENTORY MANAGEMENT ==========");
        System.out.println("1. List all products");
        System.out.println("2. Display product information");
        System.out.println("3. Add a product");
        System.out.println("4. Purchase stock");
        System.out.println("5. Ship product to customer");
        System.out.println("6. Balance stock");
        System.out.println("7. Profit and loss calculation");
        System.out.println("8. Purchase report by date range");
        System.out.println("9. Exit");
    }

    private void listProducts() {
        List<Product> products = inventory.getProducts();
        if (products.isEmpty()) {
            System.out.println("No products have been added.");
            return;
        }
        System.out.printf("%-12s %-24s %10s %12s %12s%n", "SKU", "Product", "Stock", "Unit price", "Avg. cost");
        for (Product product : products) {
            System.out.printf("%-12s %-24s %10d %12.2f %12.2f%n",
                    product.getSku(), product.getName(), product.getQuantity(),
                    product.getSellingPrice(), product.getAverageCost());
        }
    }

    private void displayProduct() {
        Product product = findProduct();
        if (product == null) return;
        System.out.println("\nSKU: " + product.getSku());
        System.out.println("Name: " + product.getName());
        System.out.printf("Selling price: $%.2f%n", product.getSellingPrice());
        System.out.printf("Average unit cost: $%.2f%n", product.getAverageCost());
        System.out.println("Quantity in stock: " + product.getQuantity());
        System.out.printf("Stock value: $%.2f%n", product.getStockValue());
    }

    private void addProduct() {
        String sku = readNonEmpty("Product SKU: ");
        if (inventory.findProduct(sku) != null) {
            System.out.println("That SKU already exists.");
            return;
        }
        String name = readNonEmpty("Product name: ");
        double sellingPrice = readPositiveAmount("Selling price per unit: ");
        inventory.addProduct(sku, name, sellingPrice);
        System.out.println("Product added. Use Purchase stock to record its initial quantity.");
    }

    private void purchase() {
        Product product = findProduct();
        if (product == null) return;
        int quantity = readPositiveInt("Quantity purchased: ");
        double unitCost = readPositiveAmount("Purchase cost per unit: ");
        Purchase purchase = inventory.purchase(product, quantity, unitCost, LocalDate.now());
        System.out.printf("Purchase recorded: %d x %s, total cost $%.2f.%n",
                purchase.getQuantity(), product.getName(), purchase.getTotalCost());
        System.out.printf("New stock: %d; weighted average unit cost: $%.2f%n",
                product.getQuantity(), product.getAverageCost());
    }

    private void ship() {
        Product product = findProduct();
        if (product == null) return;
        int quantity = readPositiveInt("Quantity to ship: ");
        try {
            Shipment shipment = inventory.ship(product, quantity, LocalDate.now());
            System.out.printf("Shipment recorded: %d x %s.%n", quantity, product.getName());
            System.out.printf("Revenue: $%.2f; cost: $%.2f; profit/loss: $%.2f%n",
                    shipment.getRevenue(), shipment.getCost(), shipment.getProfit());
            System.out.println("Stock remaining: " + product.getQuantity());
        } catch (IllegalArgumentException exception) {
            System.out.println("Shipment declined: " + exception.getMessage());
        }
    }

    private void showStockBalance() {
        List<Product> products = inventory.getProducts();
        if (products.isEmpty()) {
            System.out.println("No products have been added.");
            return;
        }
        int totalUnits = 0;
        double totalValue = 0;
        System.out.println("\n---------------- STOCK BALANCE ----------------");
        for (Product product : products) {
            System.out.printf("%-12s %-24s quantity: %d  stock value: $%.2f%n",
                    product.getSku(), product.getName(), product.getQuantity(), product.getStockValue());
            totalUnits += product.getQuantity();
            totalValue += product.getStockValue();
        }
        System.out.printf("Total units: %d | Total inventory value: $%.2f%n", totalUnits, totalValue);
    }

    private void calculateProfitLoss() {
        DateRange range = readDateRange();
        double revenue = 0;
        double cost = 0;
        for (Shipment shipment : inventory.getShipments()) {
            if (range.includes(shipment.getDate())) {
                revenue += shipment.getRevenue();
                cost += shipment.getCost();
            }
        }
        System.out.printf("\nProfit/loss from %s to %s%n", range.from, range.to);
        System.out.printf("Sales revenue: $%.2f%nCost of shipped goods: $%.2f%nProfit/loss: $%.2f%n",
                revenue, cost, revenue - cost);
    }

    private void purchaseReport() {
        DateRange range = readDateRange();
        List<Purchase> matches = new ArrayList<>();
        double total = 0;
        for (Purchase purchase : inventory.getPurchases()) {
            if (range.includes(purchase.getDate())) {
                matches.add(purchase);
                total += purchase.getTotalCost();
            }
        }
        System.out.printf("\nPurchase report from %s to %s%n", range.from, range.to);
        if (matches.isEmpty()) {
            System.out.println("No purchases found in this date range.");
            return;
        }
        System.out.printf("%-12s %-24s %12s %14s %14s %12s%n",
                "Date", "Product", "Quantity", "Unit cost", "Total cost", "SKU");
        for (Purchase purchase : matches) {
            System.out.printf("%-12s %-24s %12d %14.2f %14.2f %12s%n",
                    purchase.getDate(), purchase.getProductName(), purchase.getQuantity(),
                    purchase.getUnitCost(), purchase.getTotalCost(), purchase.getSku());
        }
        System.out.printf("Total purchases: $%.2f%n", total);
    }

    private Product findProduct() {
        String sku = readNonEmpty("Product SKU: ");
        Product product = inventory.findProduct(sku);
        if (product == null) System.out.println("No product found with that SKU.");
        return product;
    }

    private DateRange readDateRange() {
        while (true) {
            LocalDate from = readDate("From date (YYYY-MM-DD): ");
            LocalDate to = readDate("To date (YYYY-MM-DD): ");
            if (!to.isBefore(from)) return new DateRange(from, to);
            System.out.println("The end date must be the same as or later than the start date.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String value = readNonEmpty(prompt);
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException exception) {
                System.out.println("Enter a valid date in YYYY-MM-DD format.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {
                // Keep the prompt consistent for malformed and non-positive values.
            }
            System.out.println("Enter a whole number greater than zero.");
        }
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

    private double readPositiveAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(input.nextLine().trim());
                if (Double.isFinite(value) && value > 0) return value;
            } catch (NumberFormatException ignored) {
                // Keep the prompt consistent for malformed and non-positive values.
            }
            System.out.println("Enter an amount greater than zero.");
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

final class Inventory {
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final List<Purchase> purchases = new ArrayList<>();
    private final List<Shipment> shipments = new ArrayList<>();

    public void addProduct(String sku, String name, double sellingPrice) {
        products.put(sku.toUpperCase(), new Product(sku.toUpperCase(), name, sellingPrice));
    }

    public Product findProduct(String sku) {
        return products.get(sku.toUpperCase());
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products.values());
    }

    public List<Purchase> getPurchases() {
        return new ArrayList<>(purchases);
    }

    public List<Shipment> getShipments() {
        return new ArrayList<>(shipments);
    }

    public Purchase purchase(Product product, int quantity, double unitCost, LocalDate date) {
        product.addStock(quantity, unitCost);
        Purchase purchase = new Purchase(product, quantity, unitCost, date);
        purchases.add(purchase);
        return purchase;
    }

    public Shipment ship(Product product, int quantity, LocalDate date) {
        Shipment shipment = product.ship(quantity, date);
        shipments.add(shipment);
        return shipment;
    }
}

final class Product {
    private final String sku;
    private final String name;
    private final double sellingPrice;
    private int quantity;
    private double averageCost;

    Product(String sku, String name, double sellingPrice) {
        this.sku = sku;
        this.name = name;
        this.sellingPrice = sellingPrice;
    }

    void addStock(int addedQuantity, double unitCost) {
        if (addedQuantity <= 0 || !Double.isFinite(unitCost) || unitCost <= 0) {
            throw new IllegalArgumentException("Quantity and unit cost must be greater than zero.");
        }
        averageCost = ((quantity * averageCost) + (addedQuantity * unitCost)) / (quantity + addedQuantity);
        quantity += addedQuantity;
    }

    Shipment ship(int shippedQuantity, LocalDate date) {
        if (shippedQuantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (shippedQuantity > quantity) throw new IllegalArgumentException("Not enough stock available.");
        quantity -= shippedQuantity;
        return new Shipment(this, shippedQuantity, sellingPrice, averageCost, date);
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public double getSellingPrice() { return sellingPrice; }
    public int getQuantity() { return quantity; }
    public double getAverageCost() { return averageCost; }
    public double getStockValue() { return quantity * averageCost; }
}

final class Purchase {
    private final String sku;
    private final String productName;
    private final int quantity;
    private final double unitCost;
    private final LocalDate date;

    Purchase(Product product, int quantity, double unitCost, LocalDate date) {
        this.sku = product.getSku();
        this.productName = product.getName();
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.date = date;
    }

    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitCost() { return unitCost; }
    public double getTotalCost() { return quantity * unitCost; }
    public LocalDate getDate() { return date; }
}

final class Shipment {
    private final String sku;
    private final String productName;
    private final int quantity;
    private final double unitSellingPrice;
    private final double unitCost;
    private final LocalDate date;

    Shipment(Product product, int quantity, double unitSellingPrice, double unitCost, LocalDate date) {
        this.sku = product.getSku();
        this.productName = product.getName();
        this.quantity = quantity;
        this.unitSellingPrice = unitSellingPrice;
        this.unitCost = unitCost;
        this.date = date;
    }

    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public LocalDate getDate() { return date; }
    public double getRevenue() { return quantity * unitSellingPrice; }
    public double getCost() { return quantity * unitCost; }
    public double getProfit() { return getRevenue() - getCost(); }
}

final class DateRange {
    final LocalDate from;
    final LocalDate to;

    DateRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    boolean includes(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
