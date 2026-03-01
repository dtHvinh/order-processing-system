package com.dthvinh.common.storage;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Storage<T> extends AutoCloseable {
   void save(String var1, T var2);

   Optional<T> get(String var1);

   List<T> getAll(Predicate<T> var1, int var2, int var3);

   boolean exists(String var1);

   boolean delete(String var1);

   boolean isHealthy();

   void close();
}
