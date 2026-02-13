package com.dthvinh.order.mapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.dthvinh.order.api.CreateOrderItemRequest;
import com.dthvinh.order.api.CreateOrderRequest;
import com.dthvinh.order.api.OrderItemResponse;
import com.dthvinh.order.api.OrderResponse;
import com.dthvinh.order.model.Order;
import com.dthvinh.order.model.OrderItem;
import com.dthvinh.order.model.OrderStatus;

public class OrderMapper {

    public Order toOrder(CreateOrderRequest request, String orderId, Instant createdAt) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(request.getUserId());
        order.setTotalAmount(request.getTotalAmount());
        order.setCurrency(request.getCurrency());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(createdAt);

        List<OrderItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (CreateOrderItemRequest item : request.getItems()) {
                items.add(new OrderItem(item.getProductId(), item.getQuantity(), item.getUnitPrice()));
            }
        }
        order.setItems(items);

        return order;
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setCurrency(order.getCurrency());
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        response.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);

        List<OrderItemResponse> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                OrderItemResponse itemResponse = new OrderItemResponse();
                itemResponse.setProductId(item.getProductId());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitPrice(item.getUnitPrice());
                items.add(itemResponse);
            }
        }
        response.setItems(items);

        return response;
    }
}
