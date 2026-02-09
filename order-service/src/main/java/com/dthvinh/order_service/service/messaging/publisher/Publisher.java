package com.dthvinh.order_service.service.messaging.publisher;

public interface Publisher {
    void publish(String topic, String key, String value);

    <T> void publish(String topic, String key, T value);
}
