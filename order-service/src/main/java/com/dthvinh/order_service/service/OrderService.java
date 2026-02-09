package com.dthvinh.order_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dthvinh.order_service.models.CreateOrderRequest;
import com.dthvinh.order_service.models.event.OrderCreatedEvent;
import com.dthvinh.order_service.models.mapper.EventMapper;
import com.dthvinh.order_service.service.messaging.publisher.Publisher;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {
    private final Publisher publisher;

    public UUID createOrder(CreateOrderRequest request) {
        OrderCreatedEvent event = EventMapper.INSTANCE.toOrderCreatedEvent(request);
        publisher.publish("ORDER", event.getEventId().toString(), event);

        return event.getOrderId();
    }
}
