package com.dthvinh.product.model;

public class Product {
    private String productId;
    private int quantity;
    private long unitPrice;

    public Product() {
    }

    public Product(String productId, int quantity, long unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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
