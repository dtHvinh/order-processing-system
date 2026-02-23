package com.dthvinh.service.storage;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Storage<T> extends AutoCloseable {

    void save(String id, T value);

    Optional<T> get(String id);

    List<T> getAll(Predicate<T> filter, int offset, int limit);

    boolean exists(String id);

    boolean delete(String id);

    @Override
    void close();
}
