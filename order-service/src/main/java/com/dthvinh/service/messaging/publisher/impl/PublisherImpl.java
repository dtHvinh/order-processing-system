package com.dthvinh.service.messaging.publisher.impl;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dthvinh.factory.GsonFactory;
import com.dthvinh.service.messaging.publisher.Publisher;
import com.google.gson.Gson;

public class PublisherImpl implements Publisher {
    private String bootStrapServer;
    private KafkaProducer<String, String> producer;
    private final Gson mapper = GsonFactory.createGson();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PublisherImpl() {
    }

    public void init() {
        logger.info("Kafka bootstrap server = {}", bootStrapServer);

        if (bootStrapServer == null || bootStrapServer.isBlank()) {
            throw new IllegalStateException("kafka.bootstrap.server not configured");
        }

        Properties props = getConfig();
        setProducerProperties(props);
    }

    public void setBootStrapServer(String bootStrapServer) {
        this.bootStrapServer = bootStrapServer;
    }

    public Properties getConfig() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        return props;
    }

    public void setProducerProperties(Properties props) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
        producer = new KafkaProducer<>(props);
        Thread.currentThread().setContextClassLoader(context);
    }

    public void handleProducerSend(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            logger.error("Kafka send failed");
            exception.printStackTrace();
        } else {
            logger.info(
                    "Sent to topic={} partition={} offset={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset());
        }
    }

    public <T> void send(String topic, String key, T data) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key,
                mapper.toJson(data));

        producer.send(record, this::handleProducerSend);
    }
}
