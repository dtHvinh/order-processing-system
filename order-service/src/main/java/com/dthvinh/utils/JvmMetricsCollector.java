package com.dthvinh.utils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

public class JvmMetricsCollector extends Collector {

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    private final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        samples.addAll(collectMemoryMetrics());
        samples.addAll(collectGarbageCollectionMetrics());
        samples.addAll(collectThreadMetrics());
        samples.addAll(collectClassLoadingMetrics());
        samples.addAll(collectProcessMetrics());

        return samples;
    }

    private List<MetricFamilySamples> collectMemoryMetrics() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        GaugeMetricFamily usedBytes = new GaugeMetricFamily(
                "jvm_memory_bytes_used",
                "Used bytes of a given JVM memory pool.",
                Arrays.asList("area", "id"));
        GaugeMetricFamily committedBytes = new GaugeMetricFamily(
                "jvm_memory_bytes_committed",
                "Committed bytes of a given JVM memory pool.",
                Arrays.asList("area", "id"));
        GaugeMetricFamily maxBytes = new GaugeMetricFamily(
                "jvm_memory_bytes_max",
                "Max bytes of a given JVM memory pool.",
                Arrays.asList("area", "id"));
        GaugeMetricFamily initBytes = new GaugeMetricFamily(
                "jvm_memory_bytes_init",
                "Initial bytes of a given JVM memory pool.",
                Arrays.asList("area", "id"));

        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            MemoryUsage usage = memoryPoolMXBean.getUsage();
            if (usage == null) {
                continue;
            }

            String area = memoryPoolMXBean.getType() == MemoryType.HEAP ? "heap" : "nonheap";
            List<String> labels = Arrays.asList(area, memoryPoolMXBean.getName());

            usedBytes.addMetric(labels, usage.getUsed());
            committedBytes.addMetric(labels, usage.getCommitted());
            initBytes.addMetric(labels, usage.getInit());
            if (usage.getMax() >= 0) {
                maxBytes.addMetric(labels, usage.getMax());
            }
        }

        GaugeMetricFamily pendingFinalization = new GaugeMetricFamily(
                "jvm_memory_objects_pending_finalization",
                "Approximate number of objects pending finalization.",
                Collections.emptyList());
        pendingFinalization.addMetric(Collections.emptyList(), memoryMXBean.getObjectPendingFinalizationCount());

        samples.add(usedBytes);
        samples.add(committedBytes);
        samples.add(maxBytes);
        samples.add(initBytes);
        samples.add(pendingFinalization);
        return samples;
    }

    private List<MetricFamilySamples> collectGarbageCollectionMetrics() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        CounterMetricFamily gcCollectionCount = new CounterMetricFamily(
                "jvm_gc_collection_events_total",
                "Total number of JVM garbage collection events.",
                Collections.singletonList("gc"));
        CounterMetricFamily gcCollectionSeconds = new CounterMetricFamily(
                "jvm_gc_collection_seconds_total",
                "Total time spent in JVM garbage collection in seconds.",
                Collections.singletonList("gc"));

        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            List<String> labels = Collections.singletonList(garbageCollectorMXBean.getName());

            long collectionCount = garbageCollectorMXBean.getCollectionCount();
            if (collectionCount >= 0) {
                gcCollectionCount.addMetric(labels, collectionCount);
            }

            long collectionTime = garbageCollectorMXBean.getCollectionTime();
            if (collectionTime >= 0) {
                gcCollectionSeconds.addMetric(labels, collectionTime / 1000.0d);
            }
        }

        samples.add(gcCollectionCount);
        samples.add(gcCollectionSeconds);
        return samples;
    }

    private List<MetricFamilySamples> collectThreadMetrics() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        GaugeMetricFamily liveThreads = new GaugeMetricFamily(
                "jvm_threads_live_threads",
                "Current live thread count including daemon and non-daemon threads.",
                Collections.emptyList());
        GaugeMetricFamily daemonThreads = new GaugeMetricFamily(
                "jvm_threads_daemon_threads",
                "Current daemon thread count.",
                Collections.emptyList());
        GaugeMetricFamily peakThreads = new GaugeMetricFamily(
                "jvm_threads_peak_threads",
                "Peak live thread count since JVM start or thread count peak reset.",
                Collections.emptyList());
        CounterMetricFamily startedThreads = new CounterMetricFamily(
                "jvm_threads_started_total",
                "Total started thread count since JVM start.",
                Collections.emptyList());
        GaugeMetricFamily threadStates = new GaugeMetricFamily(
                "jvm_threads_state",
                "Current JVM thread count by state.",
                Collections.singletonList("state"));

        liveThreads.addMetric(Collections.emptyList(), threadMXBean.getThreadCount());
        daemonThreads.addMetric(Collections.emptyList(), threadMXBean.getDaemonThreadCount());
        peakThreads.addMetric(Collections.emptyList(), threadMXBean.getPeakThreadCount());
        startedThreads.addMetric(Collections.emptyList(), threadMXBean.getTotalStartedThreadCount());

        long[] threadIds = threadMXBean.getAllThreadIds();
        int[] stateCounts = new int[Thread.State.values().length];
        for (long threadId : threadIds) {
            Thread.State state = threadMXBean.getThreadInfo(threadId) != null
                    ? threadMXBean.getThreadInfo(threadId).getThreadState()
                    : null;
            if (state != null) {
                stateCounts[state.ordinal()]++;
            }
        }

        for (Thread.State state : Thread.State.values()) {
            threadStates.addMetric(Collections.singletonList(state.name().toLowerCase()), stateCounts[state.ordinal()]);
        }

        samples.add(liveThreads);
        samples.add(daemonThreads);
        samples.add(peakThreads);
        samples.add(startedThreads);
        samples.add(threadStates);
        return samples;
    }

    private List<MetricFamilySamples> collectClassLoadingMetrics() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        GaugeMetricFamily loadedClasses = new GaugeMetricFamily(
                "jvm_classes_loaded_classes",
                "The number of classes currently loaded in the JVM.",
                Collections.emptyList());
        CounterMetricFamily loadedClassesTotal = new CounterMetricFamily(
                "jvm_classes_loaded_total",
                "Total number of classes loaded since JVM start.",
                Collections.emptyList());
        CounterMetricFamily unloadedClassesTotal = new CounterMetricFamily(
                "jvm_classes_unloaded_total",
                "Total number of classes unloaded since JVM start.",
                Collections.emptyList());

        loadedClasses.addMetric(Collections.emptyList(), classLoadingMXBean.getLoadedClassCount());
        loadedClassesTotal.addMetric(Collections.emptyList(), classLoadingMXBean.getTotalLoadedClassCount());
        unloadedClassesTotal.addMetric(Collections.emptyList(), classLoadingMXBean.getUnloadedClassCount());

        samples.add(loadedClasses);
        samples.add(loadedClassesTotal);
        samples.add(unloadedClassesTotal);
        return samples;
    }

    private List<MetricFamilySamples> collectProcessMetrics() {
        List<MetricFamilySamples> samples = new ArrayList<>();

        GaugeMetricFamily processStartTime = new GaugeMetricFamily(
                "process_start_time_seconds",
                "Start time of the process since unix epoch in seconds.",
                Collections.emptyList());
        GaugeMetricFamily processUptime = new GaugeMetricFamily(
                "process_uptime_seconds",
                "Uptime of the Java process in seconds.",
                Collections.emptyList());
        GaugeMetricFamily availableProcessors = new GaugeMetricFamily(
                "system_cpu_available_processors",
                "The number of processors available to the JVM.",
                Collections.emptyList());
        GaugeMetricFamily systemLoadAverage = new GaugeMetricFamily(
                "system_load_average_1m",
                "System load average for the last minute.",
                Collections.emptyList());
        GaugeMetricFamily processCpuUsage = new GaugeMetricFamily(
                "process_cpu_usage",
                "Recent CPU usage for the Java process as a value between 0 and 1.",
                Collections.emptyList());
        GaugeMetricFamily systemCpuUsage = new GaugeMetricFamily(
                "system_cpu_usage",
                "Recent system CPU usage as a value between 0 and 1.",
                Collections.emptyList());
        CounterMetricFamily processCpuSeconds = new CounterMetricFamily(
                "process_cpu_seconds_total",
                "Total CPU time consumed by the Java process in seconds.",
                Collections.emptyList());
        GaugeMetricFamily processVirtualMemoryBytes = new GaugeMetricFamily(
                "process_virtual_memory_bytes",
                "Committed virtual memory size in bytes.",
                Collections.emptyList());

        processStartTime.addMetric(Collections.emptyList(),
                Instant.ofEpochMilli(runtimeMXBean.getStartTime()).getEpochSecond());
        processUptime.addMetric(Collections.emptyList(), runtimeMXBean.getUptime() / 1000.0d);
        availableProcessors.addMetric(Collections.emptyList(), operatingSystemMXBean.getAvailableProcessors());

        double loadAverage = operatingSystemMXBean.getSystemLoadAverage();
        if (loadAverage >= 0) {
            systemLoadAverage.addMetric(Collections.emptyList(), loadAverage);
        }

        Double processCpuLoadValue = invokeDoubleMethod(operatingSystemMXBean, "getProcessCpuLoad");
        if (processCpuLoadValue != null && processCpuLoadValue >= 0) {
            processCpuUsage.addMetric(Collections.emptyList(), processCpuLoadValue);
        }

        Double systemCpuLoadValue = invokeDoubleMethod(operatingSystemMXBean, "getSystemCpuLoad");
        if (systemCpuLoadValue != null && systemCpuLoadValue >= 0) {
            systemCpuUsage.addMetric(Collections.emptyList(), systemCpuLoadValue);
        }

        Long processCpuTimeValue = invokeLongMethod(operatingSystemMXBean, "getProcessCpuTime");
        if (processCpuTimeValue != null && processCpuTimeValue >= 0) {
            processCpuSeconds.addMetric(Collections.emptyList(), processCpuTimeValue / 1_000_000_000.0d);
        }

        Long committedVirtualMemory = invokeLongMethod(operatingSystemMXBean, "getCommittedVirtualMemorySize");
        if (committedVirtualMemory != null && committedVirtualMemory >= 0) {
            processVirtualMemoryBytes.addMetric(Collections.emptyList(), committedVirtualMemory);
        }

        samples.add(processStartTime);
        samples.add(processUptime);
        samples.add(availableProcessors);
        samples.add(systemLoadAverage);
        samples.add(processCpuUsage);
        samples.add(systemCpuUsage);
        samples.add(processCpuSeconds);
        samples.add(processVirtualMemoryBytes);
        return samples;
    }

    private Double invokeDoubleMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Double ? (Double) value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private Long invokeLongMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Long ? (Long) value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}