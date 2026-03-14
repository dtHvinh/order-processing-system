package com.dthvinh.inventory.model;

public class Inventory {
    private String productId;
    private int available;
    private int reserved;

    public Inventory() {
    }

    public Inventory(String productId, int available, int reserved) {
        this.productId = productId;
        this.available = available;
        this.reserved = reserved;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
}
