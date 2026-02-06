package com.dthvinh.order_service.models.event;

import lombok.Data;

@Data
public class OrderItemEvent {
    private String productId;
    private int quantity;
}
