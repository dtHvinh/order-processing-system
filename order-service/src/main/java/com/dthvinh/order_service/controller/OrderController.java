package com.dthvinh.order_service.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dthvinh.order_service.models.CreateOrderRequest;
import com.dthvinh.order_service.models.CreateOrderResponse;
import com.dthvinh.order_service.service.OrderService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        UUID orderId = orderService.createOrder(request);

        return new CreateOrderResponse() {
            {
                setOrderId(orderId.toString());
            }
        };
    }

}
