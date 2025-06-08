package com.modfive.myapplication;

public class InventoryItem {
    private String name;
    private int quantity;

    public InventoryItem(String name) {
        this.name = name;
        this.quantity = 0;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity() {
        quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 0) quantity--;
    }
}
