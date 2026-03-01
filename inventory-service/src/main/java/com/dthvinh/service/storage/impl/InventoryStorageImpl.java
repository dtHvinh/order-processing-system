package com.dthvinh.service.storage.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dthvinh.common.storage.Storage;
import com.dthvinh.inventory.model.Inventory;
import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

public class InventoryStorageImpl implements Storage<Inventory> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;
    private final Gson gson = new Gson();

    private Jedis jedis;

    private String redisServer;
    private String keyPrefix;

    public InventoryStorageImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setRedisServer(String redisServer) {
        this.redisServer = redisServer;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void init() {
        logger.info("Initializing InventoryStorageImpl with DataSource={}, redisServer={}, keyPrefix={}", dataSource, redisServer, keyPrefix);

        if (dataSource == null) {
            throw new IllegalStateException("DataSource is required");
        }
        if (redisServer == null || redisServer.isBlank()) {
            throw new IllegalStateException("redisServer is required");
        }
        if (keyPrefix == null) {
            keyPrefix = "inventory:";
        }

        String[] parts = redisServer.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid redisServer format, expected host:port");
        }

        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        this.jedis = new Jedis(host, port);

        String sql = "CREATE TABLE IF NOT EXISTS " + safeTableName() + " ("
                + "product_id VARCHAR(255) PRIMARY KEY, "
                + "available INT NOT NULL, "
                + "reserved INT NOT NULL)";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize postgres table", e);
        }
    }

    @Override
    public void save(String id, Inventory value) {
        requireId(id);
        Objects.requireNonNull(value, "value");

        String sql = "INSERT INTO " + safeTableName() + " (product_id, available, reserved) VALUES (?, ?, ?) "
                + "ON CONFLICT (product_id) DO UPDATE SET available = EXCLUDED.available, reserved = EXCLUDED.reserved";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, value.getAvailable());
            ps.setInt(3, value.getReserved());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save inventory productId=" + id, e);
        }

        try {
            jedis.set(toRedisKey(id), gson.toJson(value));
        } catch (Exception e) {
            logger.error("Failed to write inventory cache for productId={}", id);
        }
    }

    @Override
    public Optional<Inventory> get(String id) {
        requireId(id);

        try {
            String json = jedis.get(toRedisKey(id));
            if (json != null && !json.isBlank()) {
                return Optional.ofNullable(gson.fromJson(json, Inventory.class));
            }
        } catch (Exception e) {
            logger.info("Redis read failed for productId={}, falling back to Postgres", id);
        }

        String sql = "SELECT product_id, available, reserved FROM " + safeTableName() + " WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Inventory inventory = mapRow(rs);

                try {
                    jedis.set(toRedisKey(id), gson.toJson(inventory));
                } catch (Exception e) {
                    logger.info("Failed to populate inventory cache for productId={}", id);
                }

                return Optional.of(inventory);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get inventory productId=" + id, e);
        }
    }

    @Override
    public List<Inventory> getAll(Predicate<Inventory> filter, int offset, int limit) {
        int safeOffset = Integer.max(offset, 0);
        int safeLimit = Integer.max(limit, 1);

        String sql = "SELECT product_id, available, reserved FROM " + safeTableName()
                + " ORDER BY product_id OFFSET ? LIMIT ?";

        List<Inventory> result = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, safeOffset);
            ps.setInt(2, safeLimit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Inventory inventory = mapRow(rs);
                    if (filter != null && !filter.test(inventory)) {
                        continue;
                    }
                    result.add(inventory);
                }
            }

            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list inventories", e);
        }
    }

    @Override
    public boolean exists(String id) {
        requireId(id);

        String sql = "SELECT 1 FROM " + safeTableName() + " WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check existence for inventory productId=" + id, e);
        }
    }

    @Override
    public boolean delete(String id) {
        requireId(id);

        String sql = "DELETE FROM " + safeTableName() + " WHERE product_id = ?";

        boolean deleted;
        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            deleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete inventory productId=" + id, e);
        }

        try {
            jedis.del(toRedisKey(id));
        } catch (Exception e) {
            logger.info("Failed to delete inventory cache for productId={}", id);
        }

        return deleted;
    }

    @Override
    public boolean isHealthy() {
        boolean postgresOk;
        try (Connection connection = openConnection()) {
            postgresOk = connection.isValid(2);
        } catch (SQLException e) {
            postgresOk = false;
        }

        boolean redisOk;
        try {
            redisOk = "PONG".equalsIgnoreCase(jedis.ping());
        } catch (Exception e) {
            redisOk = false;
        }

        return postgresOk && redisOk;
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    private String safeTableName() {
        return "inventories";
    }

    private Connection openConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private Inventory mapRow(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setProductId(rs.getString("product_id"));
        inventory.setAvailable(rs.getInt("available"));
        inventory.setReserved(rs.getInt("reserved"));
        return inventory;
    }

    private void requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
    }

    private String toRedisKey(String id) {
        return keyPrefix + id;
    }
}
