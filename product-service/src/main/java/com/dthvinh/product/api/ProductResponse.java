package com.dthvinh.product.api;

public class ProductResponse {

    private String productId;
    private int quantity;
    private long unitPrice;

    public ProductResponse() {
    }

    public ProductResponse(String productId, int quantity, long unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }
}