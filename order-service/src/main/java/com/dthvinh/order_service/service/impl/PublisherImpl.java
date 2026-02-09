package com.dthvinh.order_service.service.impl;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dthvinh.order_service.common.Env;
import com.dthvinh.order_service.service.Publisher;
import com.dthvinh.order_service.service.PublisherErrorHandler;
import com.google.gson.Gson;

@Service
public class PublisherImpl implements Publisher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KafkaProducer<String, String> producer;
    private final Gson gson = new Gson();
    private final List<PublisherErrorHandler> errorHandlers;

    public PublisherImpl(List<PublisherErrorHandler> errorHandlers) {
        this.errorHandlers = errorHandlers;
        this.producer = new KafkaProducer<>(getDefaultProperties());
    }

    @Override
    public void publish(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

        producer.send(record, this::publishCallback);
    }

    @Override
    public void publish(String topic, String key, Object value) {
        String jsonValue = gson.toJson(value);
        publish(topic, key, jsonValue);
    }

    public Properties getDefaultProperties() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Env.KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);
        return props;
    }

    public void publishCallback(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            errorHandlers.forEach(handler -> handler.handleError(metadata, exception));
        } else {
            logger.info(
                    "Sent message to topic={} partition={} offset={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());
        }
    }
}