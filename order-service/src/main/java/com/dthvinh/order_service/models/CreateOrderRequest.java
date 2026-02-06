package com.dthvinh.order_service.models;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String userId;
    private List<OrderItemRequest> items;
    private Long totalAmount;
    private String currency = "VND";
}
