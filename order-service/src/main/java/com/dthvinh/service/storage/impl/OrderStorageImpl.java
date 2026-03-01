package com.dthvinh.service.storage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dthvinh.common.storage.Storage;
import com.dthvinh.factory.GsonFactory;
import com.dthvinh.order.model.Order;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class OrderStorageImpl implements Storage<Order> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Jedis jedis;
    private Gson gson;

    private String keyPrefix;
    private String redisServer;

    public OrderStorageImpl() {
    }

    public void setRedisServer(String redisServer) {
        this.redisServer = redisServer;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void init() {
        logger.info("Initializing OrderStorageImpl with redisServer={}, keyPrefix={}", redisServer, keyPrefix);

        String[] parts = redisServer.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid redisServer format, expected host:port");
        }

        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        this.jedis = new Jedis(host, port);
        this.gson = GsonFactory.createGson();
    }

    @Override
    public void save(String id, Order value) {
        logger.info("Saving order with id={}", id);

        String key = toKey(id);
        Objects.requireNonNull(value, "value");
        jedis.set(key, gson.toJson(value));
    }

    @Override
    public Optional<Order> get(String id) {
        logger.info("Getting order with id={}", id);

        String key = toKey(id);
        String json = jedis.get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(gson.fromJson(json, Order.class));
    }

    @Override
    public List<Order> getAll(Predicate<Order> filter, int offset, int limit) {
        logger.info("Getting all orders with filter={}, offset={}, limit={}", filter, offset, limit);

        List<Order> result = new ArrayList<>();
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams params = new ScanParams()
                .match(keyPrefix + "*")
                .count(100);
        int matched = 0;
        do {
            ScanResult<String> scanResult = jedis.scan(cursor, params);
            List<String> keys = scanResult.getResult();
            if (!keys.isEmpty()) {
                List<String> values = jedis.mget(keys.toArray(new String[0]));
                for (String json : values) {
                    if (json == null)
                        continue;
                    Order order = gson.fromJson(json, Order.class);
                    if (filter != null && !filter.test(order)) {
                        continue;
                    }
                    if (matched < offset) {
                        matched++;
                        continue;
                    }
                    if (result.size() < limit) {
                        result.add(order);
                    } else {
                        return result;
                    }
                    matched++;
                }
            }
            cursor = scanResult.getCursor();
        } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

        return result;
    }

    @Override
    public boolean exists(String id) {
        logger.info("Checking existence of order with id={}", id);

        String key = toKey(id);
        return jedis.exists(key);
    }

    @Override
    public boolean delete(String id) {
        logger.info("Deleting order with id={}", id);

        String key = toKey(id);
        return jedis.del(key) > 0;
    }

    @Override
    public void close() {
        logger.info("Closing OrderStorageImpl");

        jedis.close();
    }

    @Override
    public boolean isHealthy() {
        try {
            String response = jedis.ping();
            return "PONG".equalsIgnoreCase(response);
        } catch (Exception e) {
            logger.error("Failed to ping Redis server");
            return false;
        }
    }

    private String toKey(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        return keyPrefix + id;
    }
}
