package com.dthvinh.metrics.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;
import io.prometheus.client.SampleNameFilter;

@SuppressWarnings("unused")
public class MemoryPoolsExports extends Collector {
    private static final String JVM_MEMORY_OBJECTS_PENDING_FINALIZATION = "jvm_memory_objects_pending_finalization";
    private static final String JVM_MEMORY_BYTES_USED = "jvm_memory_bytes_used";
    private static final String JVM_MEMORY_BYTES_COMMITTED = "jvm_memory_bytes_committed";
    private static final String JVM_MEMORY_BYTES_MAX = "jvm_memory_bytes_max";
    private static final String JVM_MEMORY_BYTES_INIT = "jvm_memory_bytes_init";
    private static final String JVM_MEMORY_POOL_BYTES_USED = "jvm_memory_pool_bytes_used";
    private static final String JVM_MEMORY_POOL_BYTES_COMMITTED = "jvm_memory_pool_bytes_committed";
    private static final String JVM_MEMORY_POOL_BYTES_MAX = "jvm_memory_pool_bytes_max";
    private static final String JVM_MEMORY_POOL_BYTES_INIT = "jvm_memory_pool_bytes_init";
    private static final String JVM_MEMORY_POOL_COLLECTION_USED_BYTES = "jvm_memory_pool_collection_used_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES = "jvm_memory_pool_collection_committed_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_MAX_BYTES = "jvm_memory_pool_collection_max_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_INIT_BYTES = "jvm_memory_pool_collection_init_bytes";
    private final MemoryMXBean memoryBean;
    private final List<MemoryPoolMXBean> poolBeans;

    public MemoryPoolsExports() {
        this(ManagementFactory.getMemoryMXBean(), ManagementFactory.getMemoryPoolMXBeans());
    }

    public MemoryPoolsExports(MemoryMXBean memoryBean, List<MemoryPoolMXBean> poolBeans) {
        this.memoryBean = memoryBean;
        this.poolBeans = poolBeans;
    }

    void addMemoryAreaMetrics(List<Collector.MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
        MemoryUsage heapUsage = this.memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = this.memoryBean.getNonHeapMemoryUsage();
        if (nameFilter.test("jvm_memory_objects_pending_finalization")) {
            GaugeMetricFamily finalizer = new GaugeMetricFamily("jvm_memory_objects_pending_finalization",
                    "The number of objects waiting in the finalizer queue.",
                    (double) this.memoryBean.getObjectPendingFinalizationCount());
            sampleFamilies.add(finalizer);
        }

        if (nameFilter.test("jvm_memory_bytes_used")) {
            GaugeMetricFamily used = new GaugeMetricFamily("jvm_memory_bytes_used",
                    "Used bytes of a given JVM memory area.", Collections.singletonList("area"));
            used.addMetric(Collections.singletonList("heap"), (double) heapUsage.getUsed());
            used.addMetric(Collections.singletonList("nonheap"), (double) nonHeapUsage.getUsed());
            sampleFamilies.add(used);
        }

        if (nameFilter.test("jvm_memory_bytes_committed")) {
            GaugeMetricFamily committed = new GaugeMetricFamily("jvm_memory_bytes_committed",
                    "Committed (bytes) of a given JVM memory area.", Collections.singletonList("area"));
            committed.addMetric(Collections.singletonList("heap"), (double) heapUsage.getCommitted());
            committed.addMetric(Collections.singletonList("nonheap"), (double) nonHeapUsage.getCommitted());
            sampleFamilies.add(committed);
        }

        if (nameFilter.test("jvm_memory_bytes_max")) {
            GaugeMetricFamily max = new GaugeMetricFamily("jvm_memory_bytes_max",
                    "Max (bytes) of a given JVM memory area.", Collections.singletonList("area"));
            max.addMetric(Collections.singletonList("heap"), (double) heapUsage.getMax());
            max.addMetric(Collections.singletonList("nonheap"), (double) nonHeapUsage.getMax());
            sampleFamilies.add(max);
        }

        if (nameFilter.test("jvm_memory_bytes_init")) {
            GaugeMetricFamily init = new GaugeMetricFamily("jvm_memory_bytes_init",
                    "Initial bytes of a given JVM memory area.", Collections.singletonList("area"));
            init.addMetric(Collections.singletonList("heap"), (double) heapUsage.getInit());
            init.addMetric(Collections.singletonList("nonheap"), (double) nonHeapUsage.getInit());
            sampleFamilies.add(init);
        }

    }

    void addMemoryPoolMetrics(List<Collector.MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
        boolean anyPoolMetricPassesFilter = false;
        GaugeMetricFamily used = null;
        if (nameFilter.test("jvm_memory_pool_bytes_used")) {
            used = new GaugeMetricFamily("jvm_memory_pool_bytes_used", "Used bytes of a given JVM memory pool.",
                    Collections.singletonList("pool"));
            sampleFamilies.add(used);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily committed = null;
        if (nameFilter.test("jvm_memory_pool_bytes_committed")) {
            committed = new GaugeMetricFamily("jvm_memory_pool_bytes_committed",
                    "Committed bytes of a given JVM memory pool.", Collections.singletonList("pool"));
            sampleFamilies.add(committed);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily max = null;
        if (nameFilter.test("jvm_memory_pool_bytes_max")) {
            max = new GaugeMetricFamily("jvm_memory_pool_bytes_max", "Max bytes of a given JVM memory pool.",
                    Collections.singletonList("pool"));
            sampleFamilies.add(max);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily init = null;
        if (nameFilter.test("jvm_memory_pool_bytes_init")) {
            init = new GaugeMetricFamily("jvm_memory_pool_bytes_init", "Initial bytes of a given JVM memory pool.",
                    Collections.singletonList("pool"));
            sampleFamilies.add(init);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily collectionUsed = null;
        if (nameFilter.test("jvm_memory_pool_collection_used_bytes")) {
            collectionUsed = new GaugeMetricFamily("jvm_memory_pool_collection_used_bytes",
                    "Used bytes after last collection of a given JVM memory pool.", Collections.singletonList("pool"));
            sampleFamilies.add(collectionUsed);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily collectionCommitted = null;
        if (nameFilter.test("jvm_memory_pool_collection_committed_bytes")) {
            collectionCommitted = new GaugeMetricFamily("jvm_memory_pool_collection_committed_bytes",
                    "Committed after last collection bytes of a given JVM memory pool.",
                    Collections.singletonList("pool"));
            sampleFamilies.add(collectionCommitted);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily collectionMax = null;
        if (nameFilter.test("jvm_memory_pool_collection_max_bytes")) {
            collectionMax = new GaugeMetricFamily("jvm_memory_pool_collection_max_bytes",
                    "Max bytes after last collection of a given JVM memory pool.", Collections.singletonList("pool"));
            sampleFamilies.add(collectionMax);
            anyPoolMetricPassesFilter = true;
        }

        GaugeMetricFamily collectionInit = null;
        if (nameFilter.test("jvm_memory_pool_collection_init_bytes")) {
            collectionInit = new GaugeMetricFamily("jvm_memory_pool_collection_init_bytes",
                    "Initial after last collection bytes of a given JVM memory pool.",
                    Collections.singletonList("pool"));
            sampleFamilies.add(collectionInit);
            anyPoolMetricPassesFilter = true;
        }

        if (anyPoolMetricPassesFilter) {
            for (MemoryPoolMXBean pool : this.poolBeans) {
                MemoryUsage poolUsage = pool.getUsage();
                if (poolUsage != null) {
                    this.addPoolMetrics(used, committed, max, init, pool.getName(), poolUsage);
                }

                MemoryUsage collectionPoolUsage = pool.getCollectionUsage();
                if (collectionPoolUsage != null) {
                    this.addPoolMetrics(collectionUsed, collectionCommitted, collectionMax, collectionInit,
                            pool.getName(), collectionPoolUsage);
                }
            }
        }

    }

    private void addPoolMetrics(GaugeMetricFamily used, GaugeMetricFamily committed, GaugeMetricFamily max,
            GaugeMetricFamily init, String poolName, MemoryUsage poolUsage) {
        if (used != null) {
            used.addMetric(Collections.singletonList(poolName), (double) poolUsage.getUsed());
        }

        if (committed != null) {
            committed.addMetric(Collections.singletonList(poolName), (double) poolUsage.getCommitted());
        }

        if (max != null) {
            max.addMetric(Collections.singletonList(poolName), (double) poolUsage.getMax());
        }

        if (init != null) {
            init.addMetric(Collections.singletonList(poolName), (double) poolUsage.getInit());
        }

    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.collect((Predicate<String>) null);
    }

    public List<Collector.MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();
        this.addMemoryAreaMetrics(mfs, nameFilter == null ? SampleNameFilter.ALLOW_ALL : nameFilter);
        this.addMemoryPoolMetrics(mfs, nameFilter == null ? SampleNameFilter.ALLOW_ALL : nameFilter);
        return mfs;
    }
}
