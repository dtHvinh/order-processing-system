package com.dthvinh.metrics.service;

import io.prometheus.client.CollectorRegistry;

public class DefaultExports {
    private static boolean initialized = false;

    public DefaultExports() {
    }

    public static synchronized void initialize() {
        if (!initialized) {
            register(CollectorRegistry.defaultRegistry);
            initialized = true;
        }

    }

    public static void register(CollectorRegistry registry) {
        (new com.dthvinh.metrics.service.StandardExports()).register(registry);
        (new com.dthvinh.metrics.service.MemoryPoolsExports()).register(registry);
        (new com.dthvinh.metrics.service.MemoryAllocationExports()).register(registry);
        (new com.dthvinh.metrics.service.BufferPoolsExports()).register(registry);
        (new com.dthvinh.metrics.service.GarbageCollectorExports()).register(registry);
        (new com.dthvinh.metrics.service.ThreadExports()).register(registry);
        (new com.dthvinh.metrics.service.ClassLoadingExports()).register(registry);
        (new com.dthvinh.metrics.service.VersionInfoExports()).register(registry);
    }
}
