package com.dthvinh.metrics.service;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;
import io.prometheus.client.SampleNameFilter;

@SuppressWarnings("unused")
public class ThreadExports extends Collector {
    public static final String UNKNOWN = "UNKNOWN";
    public static final String JVM_THREADS_STATE = "jvm_threads_state";
    private static final String JVM_THREADS_CURRENT = "jvm_threads_current";
    private static final String JVM_THREADS_DAEMON = "jvm_threads_daemon";
    private static final String JVM_THREADS_PEAK = "jvm_threads_peak";
    private static final String JVM_THREADS_STARTED_TOTAL = "jvm_threads_started_total";
    private static final String JVM_THREADS_DEADLOCKED = "jvm_threads_deadlocked";
    private static final String JVM_THREADS_DEADLOCKED_MONITOR = "jvm_threads_deadlocked_monitor";
    private final ThreadMXBean threadBean;

    public ThreadExports() {
        this(ManagementFactory.getThreadMXBean());
    }

    public ThreadExports(ThreadMXBean threadBean) {
        this.threadBean = threadBean;
    }

    void addThreadMetrics(List<Collector.MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
        if (nameFilter.test("jvm_threads_current")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_threads_current", "Current thread count of a JVM",
                    (double) this.threadBean.getThreadCount()));
        }

        if (nameFilter.test("jvm_threads_daemon")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_threads_daemon", "Daemon thread count of a JVM",
                    (double) this.threadBean.getDaemonThreadCount()));
        }

        if (nameFilter.test("jvm_threads_peak")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_threads_peak", "Peak thread count of a JVM",
                    (double) this.threadBean.getPeakThreadCount()));
        }

        if (nameFilter.test("jvm_threads_started_total")) {
            sampleFamilies.add(new CounterMetricFamily("jvm_threads_started_total", "Started thread count of a JVM",
                    (double) this.threadBean.getTotalStartedThreadCount()));
        }

        if (nameFilter.test("jvm_threads_deadlocked")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_threads_deadlocked",
                    "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers",
                    nullSafeArrayLength(this.threadBean.findDeadlockedThreads())));
        }

        if (nameFilter.test("jvm_threads_deadlocked_monitor")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_threads_deadlocked_monitor",
                    "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors",
                    nullSafeArrayLength(this.threadBean.findMonitorDeadlockedThreads())));
        }

        if (nameFilter.test("jvm_threads_state")) {
            GaugeMetricFamily threadStateFamily = new GaugeMetricFamily("jvm_threads_state",
                    "Current count of threads by state", Collections.singletonList("state"));
            Map<String, Integer> threadStateCounts = this.getThreadStateCountMap();

            for (Map.Entry<String, Integer> entry : threadStateCounts.entrySet()) {
                threadStateFamily.addMetric(Collections.singletonList(entry.getKey()),
                        (double) (Integer) entry.getValue());
            }

            sampleFamilies.add(threadStateFamily);
        }

    }

    private Map<String, Integer> getThreadStateCountMap() {
        long[] threadIds = this.threadBean.getAllThreadIds();
        int writePos = 0;

        for (int i = 0; i < threadIds.length; ++i) {
            if (threadIds[i] > 0L) {
                threadIds[writePos++] = threadIds[i];
            }
        }

        int numberOfInvalidThreadIds = threadIds.length - writePos;
        threadIds = Arrays.copyOf(threadIds, writePos);
        ThreadInfo[] allThreads = this.threadBean.getThreadInfo(threadIds, 0);
        HashMap<String, Integer> threadCounts = new HashMap<>();

        for (Thread.State state : State.values()) {
            threadCounts.put(state.name(), 0);
        }

        for (ThreadInfo curThread : allThreads) {
            if (curThread != null) {
                Thread.State threadState = curThread.getThreadState();
                threadCounts.put(threadState.name(), (Integer) threadCounts.get(threadState.name()) + 1);
            }
        }

        threadCounts.put("UNKNOWN", numberOfInvalidThreadIds);
        return threadCounts;
    }

    private static double nullSafeArrayLength(long[] array) {
        return null == array ? (double) 0.0F : (double) array.length;
    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.collect((Predicate<String>) null);
    }

    public List<Collector.MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();
        this.addThreadMetrics(mfs, nameFilter == null ? SampleNameFilter.ALLOW_ALL : nameFilter);
        return mfs;
    }
}
