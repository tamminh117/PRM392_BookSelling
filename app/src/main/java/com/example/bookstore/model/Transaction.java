package com.example.bookstore.model;

import java.util.List;

public class Transaction {
    private String transactionId;
    private String userId;
    private double totalPrice;
    private List<CartItem> items;
    private long transactionDate;
    private String deliveryAddress; // Thêm địa chỉ giao hàng

    public Transaction() {}

    public Transaction(String transactionId, String userId, double totalPrice, List<CartItem> items, long transactionDate, String deliveryAddress) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.items = items;
        this.transactionDate = transactionDate;
        this.deliveryAddress = deliveryAddress;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(long transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    // Các getter và setter
    // ...
}
