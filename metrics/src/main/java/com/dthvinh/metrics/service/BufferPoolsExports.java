package com.dthvinh.metrics.service;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;
import io.prometheus.client.SampleNameFilter;

@SuppressWarnings("unused")
public class BufferPoolsExports extends Collector {
    private static final String JVM_BUFFER_POOL_USED_BYTES = "jvm_buffer_pool_used_bytes";
    private static final String JVM_BUFFER_POOL_CAPACITY_BYTES = "jvm_buffer_pool_capacity_bytes";
    private static final String JVM_BUFFER_POOL_USED_BUFFERS = "jvm_buffer_pool_used_buffers";
    private static final Logger LOGGER = Logger.getLogger(BufferPoolsExports.class.getName());
    private final List<Object> bufferPoolMXBeans = new ArrayList<>();
    private Method getName;
    private Method getMemoryUsed;
    private Method getTotalCapacity;
    private Method getCount;

    public BufferPoolsExports() {
        try {
            Class<?> bufferPoolMXBeanClass = Class.forName("java.lang.management.BufferPoolMXBean");
            this.bufferPoolMXBeans.addAll(accessBufferPoolMXBeans(bufferPoolMXBeanClass));
            this.getName = bufferPoolMXBeanClass.getMethod("getName");
            this.getMemoryUsed = bufferPoolMXBeanClass.getMethod("getMemoryUsed");
            this.getTotalCapacity = bufferPoolMXBeanClass.getMethod("getTotalCapacity");
            this.getCount = bufferPoolMXBeanClass.getMethod("getCount");
        } catch (ClassNotFoundException var2) {
            LOGGER.fine("BufferPoolMXBean not available, no metrics for buffer pools will be exported");
        } catch (NoSuchMethodException e) {
            LOGGER.fine("Can not get necessary accessor from BufferPoolMXBean: " + e.getMessage());
        }

    }

    private static List<Object> accessBufferPoolMXBeans(Class<?> bufferPoolMXBeanClass) {
        try {
            Method getPlatformMXBeansMethod = ManagementFactory.class.getMethod("getPlatformMXBeans", Class.class);
            Object listOfBufferPoolMXBeanInstances = getPlatformMXBeansMethod.invoke((Object) null,
                    bufferPoolMXBeanClass);
            return (List<Object>) listOfBufferPoolMXBeanInstances;
        } catch (NoSuchMethodException var3) {
            LOGGER.fine(
                    "ManagementFactory.getPlatformMXBeans not available, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        } catch (IllegalAccessException var4) {
            LOGGER.fine(
                    "ManagementFactory.getPlatformMXBeans not accessible, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        } catch (InvocationTargetException var5) {
            LOGGER.warning(
                    "ManagementFactory.getPlatformMXBeans could not be invoked, no metrics for buffer pools will be exported");
            return Collections.emptyList();
        }
    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.collect((Predicate<String>) null);
    }

    public List<Collector.MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();
        if (nameFilter == null) {
            nameFilter = SampleNameFilter.ALLOW_ALL;
        }

        GaugeMetricFamily used = null;
        if (nameFilter.test("jvm_buffer_pool_used_bytes")) {
            used = new GaugeMetricFamily("jvm_buffer_pool_used_bytes", "Used bytes of a given JVM buffer pool.",
                    Collections.singletonList("pool"));
            mfs.add(used);
        }

        GaugeMetricFamily capacity = null;
        if (nameFilter.test("jvm_buffer_pool_capacity_bytes")) {
            capacity = new GaugeMetricFamily("jvm_buffer_pool_capacity_bytes",
                    "Bytes capacity of a given JVM buffer pool.", Collections.singletonList("pool"));
            mfs.add(capacity);
        }

        GaugeMetricFamily buffers = null;
        if (nameFilter.test("jvm_buffer_pool_used_buffers")) {
            buffers = new GaugeMetricFamily("jvm_buffer_pool_used_buffers", "Used buffers of a given JVM buffer pool.",
                    Collections.singletonList("pool"));
            mfs.add(buffers);
        }

        for (Object pool : this.bufferPoolMXBeans) {
            if (used != null) {
                used.addMetric(Collections.singletonList(this.getName(pool)),
                        (double) this.callLongMethod(this.getMemoryUsed, pool));
            }

            if (capacity != null) {
                capacity.addMetric(Collections.singletonList(this.getName(pool)),
                        (double) this.callLongMethod(this.getTotalCapacity, pool));
            }

            if (buffers != null) {
                buffers.addMetric(Collections.singletonList(this.getName(pool)),
                        (double) this.callLongMethod(this.getCount, pool));
            }
        }

        return mfs;
    }

    private long callLongMethod(Method method, Object pool) {
        try {
            return (Long) method.invoke(pool);
        } catch (IllegalAccessException e) {
            LOGGER.fine("Couldn't call " + method.getName() + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.fine("Couldn't call " + method.getName() + ": " + e.getMessage());
        }

        return 0L;
    }

    private String getName(Object pool) {
        try {
            return (String) this.getName.invoke(pool);
        } catch (IllegalAccessException e) {
            LOGGER.fine("Couldn't call getName " + e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.fine("Couldn't call getName " + e.getMessage());
        }

        return "<unknown>";
    }
}
