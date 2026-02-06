package com.dthvinh.order_service.models;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String productId;
    private int quantity;
    private Long price;
}
