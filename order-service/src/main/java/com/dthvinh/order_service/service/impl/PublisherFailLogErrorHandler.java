package com.dthvinh.order_service.service.impl;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dthvinh.order_service.service.PublisherErrorHandler;

@Component
public class PublisherFailLogErrorHandler implements PublisherErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handleError(RecordMetadata metadata, Exception exception) {
        logger.error(
                "Failed to send message to topic={} partition={}",
                metadata.topic(),
                metadata.partition(),
                exception);
    }
}
