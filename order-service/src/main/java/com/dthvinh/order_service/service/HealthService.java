package com.dthvinh.order_service.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {
    public HealthResult checkHealth() {
        return HealthResult.healthy();
    }

    public static class HealthResult {
        public boolean isHealthy;
        public String message;

        public HealthResult() {
            this.isHealthy = false;
            this.message = "Health check not implemented";
        }

        public static HealthResult healthy() {
            HealthResult result = new HealthResult();
            result.isHealthy = true;
            result.message = "Service is healthy";
            return result;
        }
    }
}
