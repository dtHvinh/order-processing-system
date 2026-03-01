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

import com.dthvinh.product.model.Product;
import com.dthvinh.service.storage.Storage;

public class ProductStorageImpl implements Storage<Product> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DataSource dataSource;

    public ProductStorageImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void init() {
        logger.info("Initializing ProductStorageImpl with OSGi DataSource={}", dataSource);
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is required");
        }

        try (Connection connection = openConnection()) {
            if (!connection.isValid(2)) {
                throw new IllegalStateException("Postgres connection is not valid");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to validate postgres connection", e);
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + safeTableName() + " ("
                + "product_id VARCHAR(255) PRIMARY KEY, "
                + "quantity INT NOT NULL, "
                + "unit_price BIGINT NOT NULL)";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize postgres table", e);
        }
    }

    @Override
    public void save(String id, Product value) {
        requireId(id);
        Objects.requireNonNull(value, "value");

        String sql = "INSERT INTO " + safeTableName() + " (product_id, quantity, unit_price) VALUES (?, ?, ?) "
                + "ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, unit_price = EXCLUDED.unit_price";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setInt(2, value.getQuantity());
            ps.setLong(3, value.getUnitPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save product id=" + id, e);
        }
    }

    @Override
    public Optional<Product> get(String id) {
        requireId(id);

        String sql = "SELECT product_id, quantity, unit_price FROM " + safeTableName() + " WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get product id=" + id, e);
        }
    }

    @Override
    public List<Product> getAll(Predicate<Product> filter, int offset, int limit) {
        int safeOffset = Integer.max(offset, 0);
        int safeLimit = Integer.max(limit, 1);

        String sql = "SELECT product_id, quantity, unit_price FROM " + safeTableName()
                + " ORDER BY product_id OFFSET ? LIMIT ?";

        List<Product> result = new ArrayList<>();
        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, safeOffset);
            ps.setInt(2, safeLimit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = mapRow(rs);
                    if (filter != null && !filter.test(product)) {
                        continue;
                    }
                    result.add(product);
                }
            }

            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list products", e);
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
            throw new IllegalStateException("Failed to check existence for product id=" + id, e);
        }
    }

    @Override
    public boolean delete(String id) {
        requireId(id);

        String sql = "DELETE FROM " + safeTableName() + " WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete product id=" + id, e);
        }
    }

    @Override
    public boolean isHealthy() {
        try (Connection connection = openConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            logger.error("Postgres health check failed");
            return false;
        }
    }

    @Override
    public void close() {
        // No-op: this impl uses short-lived connections.
    }

    /**
     * Updates the quantity to an exact value.
     */
    public boolean updateQuantity(String productId, int quantity) {
        requireId(productId);

        String sql = "UPDATE " + safeTableName() + " SET quantity = ? WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update quantity for product id=" + productId, e);
        }
    }

    /**
     * Atomically increments quantity by {@code delta}.
     */
    public boolean incrementQuantity(String productId, int delta) {
        requireId(productId);

        String sql = "UPDATE " + safeTableName() + " SET quantity = quantity + ? WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to increment quantity for product id=" + productId, e);
        }
    }

    public boolean updateUnitPrice(String productId, long unitPrice) {
        requireId(productId);

        String sql = "UPDATE " + safeTableName() + " SET unit_price = ? WHERE product_id = ?";

        try (Connection connection = openConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, unitPrice);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update unit price for product id=" + productId, e);
        }
    }

    private String safeTableName() {
        return "products";
    }

    private Connection openConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setQuantity(rs.getInt("quantity"));
        product.setUnitPrice(rs.getLong("unit_price"));
        return product;
    }

    private void requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
    }
}
