package com.dthvinh.inventory.api;

public class InventoryResponse {
    private String productId;
    private int available;
    private int reserved;

    public InventoryResponse() {
    }

    public InventoryResponse(String productId, int available, int reserved) {
        this.productId = productId;
        this.available = available;
        this.reserved = reserved;
    }

    public String getProductId() {
        return productId;
    }

    public int getAvailable() {
        return available;
    }

    public int getReserved() {
        return reserved;
    }
}
