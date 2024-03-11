package org.example.models;

import java.util.Date;
import java.util.List;

public class Order {
    int orderId;
    Date date;
    List<ItemOrder> itemOrderList;

    public Order(int orderId, List<ItemOrder> itemOrderList) {
        this.orderId = orderId;
        this.itemOrderList = itemOrderList;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ItemOrder> getItemOrderList() {
        return itemOrderList;
    }

    public void setItemOrderList(List<ItemOrder> itemOrderList) {
        this.itemOrderList = itemOrderList;
    }


}
