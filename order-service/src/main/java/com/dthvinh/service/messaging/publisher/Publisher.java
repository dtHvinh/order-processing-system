package com.dthvinh.service.messaging.publisher;

public interface Publisher {
    <T> void send(String topic, String key, T data);
}
