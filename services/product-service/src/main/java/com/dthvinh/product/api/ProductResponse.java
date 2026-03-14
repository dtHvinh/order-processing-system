package com.dthvinh.product.api;

public class ProductResponse {

    private String productId;
    private String name;
    private long unitPrice;

    public ProductResponse() {
    }

    public ProductResponse(String productId, String name, long unitPrice) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public long getUnitPrice() {
        return unitPrice;
    }
}