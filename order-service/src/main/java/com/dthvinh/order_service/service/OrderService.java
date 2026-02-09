package com.dthvinh.order_service.service;

import org.springframework.stereotype.Service;

import com.dthvinh.order_service.models.CreateOrderRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {
    private final Publisher publisher;

    public void createOrder(CreateOrderRequest request) {

    }
}
