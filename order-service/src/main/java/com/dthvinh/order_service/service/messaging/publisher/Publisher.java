package com.dthvinh.order_service.service.messaging.publisher;

public interface Publisher {
    void publish(String topic, String key, String value);

    void publish(String topic, String key, Object value);
}
