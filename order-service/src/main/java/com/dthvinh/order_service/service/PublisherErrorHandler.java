package com.dthvinh.order_service.service;

import org.apache.kafka.clients.producer.RecordMetadata;

public interface PublisherErrorHandler {
    void handleError(RecordMetadata metadata, Exception exception);
}