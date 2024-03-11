package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String name;
    private int balance;
    private List<Order> orderList;

    public Customer(String name, int balance) {
        this.name = name;
        this.balance = balance;
        this.orderList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getBalance() {
        return balance;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public void addOrder(Order order) {
        orderList.add(order);
    }

    public void displayOrders() {
        System.out.println("Orders for Customer " + name + ":");
        if (orderList.isEmpty()) {
            System.out.println("No orders available.");
        } else {
            for (Order order : orderList) {
                System.out.println(order);
            }
        }
    }

    public void updateBalance(int amount) {
        balance += amount;
    }

    @Override
    public String toString() {
        return "Customer{name='" + name + "', balance=" + balance + '}';
    }

    public void setBalance(int newBalance) {
        balance = newBalance;

    }
}
