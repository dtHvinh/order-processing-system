package com.dthvinh.order_service.models.event;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class OrderCreatedEvent {
    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItemEvent> items;
    private Long totalAmount;
    private String currency;
    private Instant createdAt;
}
