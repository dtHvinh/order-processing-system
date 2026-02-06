package com.dthvinh.order_service.models;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private String orderId;
    private String status;
    private String message;
}
