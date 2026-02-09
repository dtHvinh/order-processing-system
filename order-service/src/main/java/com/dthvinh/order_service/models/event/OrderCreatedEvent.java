package com.dthvinh.order_service.models.event;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String eventId;
    private String orderId;
    private String userId;
    private List<OrderItemEvent> items;
    private Long totalAmount;
    private String currency;
    private Instant createdAt;
}
