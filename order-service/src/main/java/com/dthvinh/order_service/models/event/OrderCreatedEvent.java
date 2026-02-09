package com.dthvinh.order_service.models.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private UUID eventId;
    private UUID orderId;
    private String userId;
    private List<OrderItemEvent> items;
    private Long totalAmount;
    private String currency;
    private Instant createdAt;
}
