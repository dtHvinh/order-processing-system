package com.dthvinh.metrics.service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;

public class MemoryAllocationExports extends Collector {
    private final Counter allocatedCounter = ((Counter.Builder) ((Counter.Builder) ((Counter.Builder) Counter.build()
            .name("jvm_memory_pool_allocated_bytes_total"))
            .help("Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously."))
            .labelNames(new String[] { "pool" })).create();

    public MemoryAllocationExports() {
        AllocationCountingNotificationListener listener = new AllocationCountingNotificationListener(
                this.allocatedCounter);

        for (GarbageCollectorMXBean garbageCollectorMXBean : this.getGarbageCollectorMXBeans()) {
            if (garbageCollectorMXBean instanceof NotificationEmitter) {
                ((NotificationEmitter) garbageCollectorMXBean).addNotificationListener(listener,
                        (NotificationFilter) null, (Object) null);
            }
        }

    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.allocatedCounter.collect();
    }

    protected List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    static class AllocationCountingNotificationListener implements NotificationListener {
        private final Map<String, Long> lastMemoryUsage = new HashMap<>();
        private final Counter counter;

        AllocationCountingNotificationListener(Counter counter) {
            this.counter = counter;
        }

        public synchronized void handleNotification(Notification notification, Object handback) {
            Object userData = notification.getUserData();
            if (!(userData instanceof CompositeData)) {
                return;
            }

            Object info = getGarbageCollectionNotificationInfo((CompositeData) userData);
            Object gcInfo = invokeMethod(info, "getGcInfo");
            Map<String, MemoryUsage> memoryUsageBeforeGc = invokeMemoryUsageMap(gcInfo, "getMemoryUsageBeforeGc");
            Map<String, MemoryUsage> memoryUsageAfterGc = invokeMemoryUsageMap(gcInfo, "getMemoryUsageAfterGc");
            if (memoryUsageBeforeGc == null || memoryUsageAfterGc == null) {
                return;
            }

            for (Map.Entry<String, MemoryUsage> entry : memoryUsageBeforeGc.entrySet()) {
                String memoryPool = (String) entry.getKey();
                long before = ((MemoryUsage) entry.getValue()).getUsed();
                long after = ((MemoryUsage) memoryUsageAfterGc.get(memoryPool)).getUsed();
                this.handleMemoryPool(memoryPool, before, after);
            }

        }

        void handleMemoryPool(String memoryPool, long before, long after) {
            long last = getAndSet(this.lastMemoryUsage, memoryPool, after);
            long diff1 = before - last;
            long diff2 = after - before;
            if (diff1 < 0L) {
                diff1 = 0L;
            }

            if (diff2 < 0L) {
                diff2 = 0L;
            }

            long increase = diff1 + diff2;
            if (increase > 0L) {
                ((Counter.Child) this.counter.labels(new String[] { memoryPool })).inc((double) increase);
            }

        }

        private static long getAndSet(Map<String, Long> map, String key, long value) {
            Long last = (Long) map.put(key, value);
            return last == null ? 0L : last;
        }

        private static Object getGarbageCollectionNotificationInfo(CompositeData userData) {
            try {
                Class<?> notificationInfoClass = Class.forName("com.sun.management.GarbageCollectionNotificationInfo");
                Method fromMethod = notificationInfoClass.getMethod("from", CompositeData.class);
                return fromMethod.invoke(null, userData);
            } catch (ReflectiveOperationException | LinkageError e) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        private static Map<String, MemoryUsage> invokeMemoryUsageMap(Object target, String methodName) {
            Object value = invokeMethod(target, methodName);
            return value instanceof Map ? (Map<String, MemoryUsage>) value : null;
        }

        private static Object invokeMethod(Object target, String methodName) {
            if (target == null) {
                return null;
            }

            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (ReflectiveOperationException | LinkageError e) {
                return null;
            }
        }
    }
}
