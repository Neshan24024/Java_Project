package org.example;

import org.example.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String DATABASE_URL = "jdbc:sqlite:canteen.db";

    public static void main(String[] args) {
        createTablesIfNotExist();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nChoose an action:");
            System.out.println("1. Add Customer");
            System.out.println("2. Place Order");
            System.out.println("3. Display Customer Orders");
            System.out.println("4. Update Customer Balance");
            System.out.println("5. Delete Order");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    addCustomer(scanner);
                    break;
                case 2:
                    placeOrder(scanner);
                    break;
                case 3:
                    displayCustomerOrders();
                    break;
                case 4:
                    updateCustomerBalance(scanner);
                    break;
                case 5:
                    deleteOrder(scanner);
                    break;
                case 6:
                    System.out.println("Exiting program. Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        }
    }

    private static void createTablesIfNotExist() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            String createCustomerTable = "CREATE TABLE IF NOT EXISTS Customer (" +
                    "name TEXT NOT NULL, " +
                    "balance INTEGER NOT NULL)";
            statement.executeUpdate(createCustomerTable);

            String createItemTable = "CREATE TABLE IF NOT EXISTS Item (" +
                    "name TEXT NOT NULL, " +
                    "price INTEGER NOT NULL)";
            statement.executeUpdate(createItemTable);

            String createOrderTable = "CREATE TABLE IF NOT EXISTS OrderTable (" +
                    "orderId INTEGER PRIMARY KEY, " +
                    "date TEXT NOT NULL)";
            statement.executeUpdate(createOrderTable);

            String createItemOrderTable = "CREATE TABLE IF NOT EXISTS ItemOrder (" +
                    "orderId INTEGER, " +
                    "itemName TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "FOREIGN KEY (orderId) REFERENCES OrderTable(orderId), " +
                    "FOREIGN KEY (itemName) REFERENCES Item(name))";
            statement.executeUpdate(createItemOrderTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addCustomer(Scanner scanner) {
        System.out.println("Enter customer name:");
        String customerName = scanner.nextLine();
        System.out.println("Enter customer balance:");
        int customerBalance = scanner.nextInt();

        Customer customer = new Customer(customerName, customerBalance);
        insertCustomer(customer);
        System.out.println("Customer added successfully!");
    }

    private static void placeOrder(Scanner scanner) {
        System.out.println("Enter customer name:");
        String customerName = scanner.nextLine();

        Customer customer = getCustomerByName(customerName);
        if (customer == null) {
            System.out.println("Customer not found. Please add the customer first.");
            return;
        }

        List<ItemOrder> itemOrderList = new ArrayList<>();
        while (true) {
            System.out.println("Enter item name (or type 'done' to finish):");
            String itemName = scanner.nextLine();

            if (itemName.equalsIgnoreCase("done")) {
                break;
            }

            System.out.println("Enter quantity:");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            Item item = getItemByName(itemName);
            if (item == null) {
                System.out.println("Item not found. Please add the item first.");
                return;
            }

            itemOrderList.add(new ItemOrder(item, quantity));
        }

        Order order = new Order(generateOrderId(), itemOrderList);
        insertOrder(order, customer);
        System.out.println("Order placed successfully!");
    }

    private static void displayCustomerOrders() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM Customer")) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int balance = resultSet.getInt("balance");
                System.out.println("Customer: " + name + ", Balance: " + balance);
            }

            ResultSet orderResultSet = statement.executeQuery("SELECT * FROM OrderTable");
            while (orderResultSet.next()) {
                int orderId = orderResultSet.getInt("orderId");
                String date = orderResultSet.getString("date");
                System.out.println("Order: " + orderId + ", Date: " + date);

                ResultSet itemOrderResultSet = statement.executeQuery("SELECT * FROM ItemOrder WHERE orderId = " + orderId);
                while (itemOrderResultSet.next()) {
                    String itemName = itemOrderResultSet.getString("itemName");
                    int quantity = itemOrderResultSet.getInt("quantity");
                    System.out.println("   Item: " + itemName + ", Quantity: " + quantity);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateCustomerBalance(Scanner scanner) {
        System.out.println("Enter customer name:");
        String customerName = scanner.nextLine();

        Customer customer = getCustomerByName(customerName);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Enter new balance for customer:");
        int newBalance = scanner.nextInt();
        customer.setBalance(newBalance);
        updateCustomer(customer);
        System.out.println("Customer balance updated successfully!");
    }

    private static void updateCustomer(Customer customer) {
    }

    private static void deleteOrder(Scanner scanner) {
        System.out.println("Enter customer name:");
        String customerName = scanner.nextLine();

        Customer customer = getCustomerByName(customerName);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println("Enter order ID to delete:");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        Order order = getOrderById(orderId);
        if (order == null) {
            System.out.println("Order not found.");
            return;
        }

        deleteOrder(order);
        System.out.println("Order deleted successfully!");
    }

    private static int generateOrderId() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT MAX(orderId) AS maxId FROM OrderTable")) {
            if (resultSet.next()) {
                return resultSet.getInt("maxId") + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static Customer getCustomerByName(String name) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Customer WHERE name = ?")) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String customerName = resultSet.getString("name");
                int balance = resultSet.getInt("balance");
                return new Customer(customerName, balance);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Item getItemByName(String name) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Item WHERE name = ?")) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String itemName = resultSet.getString("name");
                int price = resultSet.getInt("price");
                return new Item(itemName, price);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Order getOrderById(int orderId) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM OrderTable WHERE orderId = ?")) {
            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                List<ItemOrder> itemOrderList = getItemOrdersByOrderId(orderId);
                return new Order(orderId, itemOrderList);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<ItemOrder> getItemOrdersByOrderId(int orderId) {
        List<ItemOrder> itemOrderList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ItemOrder WHERE orderId = ?")) {
            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String itemName = resultSet.getString("itemName");
                int quantity = resultSet.getInt("quantity");
                Item item = getItemByName(itemName);

                if (item != null) {
                    itemOrderList.add(new ItemOrder(item, quantity));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itemOrderList;
    }

    private static void insertCustomer(Customer customer) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Customer(name, balance) VALUES (?, ?)")) {
            preparedStatement.setString(1, customer.getName());
            preparedStatement.setInt(2, customer.getBalance());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertOrder(Order order, Customer customer) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement orderStatement = connection.prepareStatement("INSERT INTO OrderTable(orderId, date) VALUES (?, ?)")) {
            orderStatement.setInt(1, order.getOrderId());
            orderStatement.setString(2, new Date().toString());
            orderStatement.executeUpdate();

            for (ItemOrder itemOrder : order.getItemOrderList()) {
                insertItemOrder(order.getOrderId(), itemOrder.getItem().getName(), itemOrder.getQuantity());
            }

            updateCustomerBalance(customer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertItemOrder(int orderId, String itemName, int quantity) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ItemOrder(orderId, itemName, quantity) VALUES (?, ?, ?)")) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setString(2, itemName);
            preparedStatement.setInt(3, quantity);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateCustomerBalance(Customer customer) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Customer SET balance = ? WHERE name = ?")) {
            preparedStatement.setInt(1, customer.getBalance());
            preparedStatement.setString(2, customer.getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteOrder(Order order) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM OrderTable WHERE orderId = ?")) {
            preparedStatement.setInt(1, order.getOrderId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
