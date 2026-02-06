package com.dthvinh.order_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dthvinh.order_service.service.HealthService;
import com.dthvinh.order_service.type.Response;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class HealthCheckController {

    private final HealthService healthService;

    @GetMapping("healthz")
    public Response getHealth() {
        return Response.ok(healthService.checkHealth());
    }
}
