package com.dthvinh.order_service.models.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderCreatedEvent {
    private UUID eventId;
    private UUID orderId;
    private String userId;
    private List<OrderItemEvent> items;
    private Long totalAmount;
    private String currency;
    private Instant createdAt;

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(UUID eventId, UUID orderId, String userId, List<OrderItemEvent> items, Long totalAmount,
            String currency, Instant createdAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItemEvent> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEvent> items) {
        this.items = items;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
