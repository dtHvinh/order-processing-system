package com.dthvinh.metrics.service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.Predicate;
import io.prometheus.client.SummaryMetricFamily;

@SuppressWarnings("unused")
public class GarbageCollectorExports extends Collector {
    private static final String JVM_GC_COLLECTION_SECONDS = "jvm_gc_collection_seconds";
    private final List<GarbageCollectorMXBean> garbageCollectors;

    public GarbageCollectorExports() {
        this(ManagementFactory.getGarbageCollectorMXBeans());
    }

    GarbageCollectorExports(List<GarbageCollectorMXBean> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.collect((Predicate<String>) null);
    }

    public List<Collector.MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();
        if (nameFilter == null || nameFilter.test("jvm_gc_collection_seconds")) {
            SummaryMetricFamily gcCollection = new SummaryMetricFamily("jvm_gc_collection_seconds",
                    "Time spent in a given JVM garbage collector in seconds.", Collections.singletonList("gc"));

            for (GarbageCollectorMXBean gc : this.garbageCollectors) {
                gcCollection.addMetric(Collections.singletonList(gc.getName()), (double) gc.getCollectionCount(),
                        (double) gc.getCollectionTime() / (double) 1000.0F);
            }

            mfs.add(gcCollection);
        }

        return mfs;
    }
}
