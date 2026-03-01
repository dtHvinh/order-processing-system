package com.dthvinh.product.api;

public class UpdateProductRequest {
    private int quantity;
    private long unitPrice;

    public UpdateProductRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }
}