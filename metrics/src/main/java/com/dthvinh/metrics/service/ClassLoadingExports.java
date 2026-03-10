package com.dthvinh.metrics.service;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;
import io.prometheus.client.SampleNameFilter;

@SuppressWarnings("unused")
public class ClassLoadingExports extends Collector {
    private static final String JVM_CLASSES_CURRENTLY_LOADED = "jvm_classes_currently_loaded";
    private static final String JVM_CLASSES_LOADED_TOTAL = "jvm_classes_loaded_total";
    private static final String JVM_CLASSES_UNLOADED_TOTAL = "jvm_classes_unloaded_total";
    private final ClassLoadingMXBean clBean;

    public ClassLoadingExports() {
        this(ManagementFactory.getClassLoadingMXBean());
    }

    public ClassLoadingExports(ClassLoadingMXBean clBean) {
        this.clBean = clBean;
    }

    void addClassLoadingMetrics(List<Collector.MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
        if (nameFilter.test("jvm_classes_currently_loaded")) {
            sampleFamilies.add(new GaugeMetricFamily("jvm_classes_currently_loaded",
                    "The number of classes that are currently loaded in the JVM",
                    (double) this.clBean.getLoadedClassCount()));
        }

        if (nameFilter.test("jvm_classes_loaded_total")) {
            sampleFamilies.add(new CounterMetricFamily("jvm_classes_loaded_total",
                    "The total number of classes that have been loaded since the JVM has started execution",
                    (double) this.clBean.getTotalLoadedClassCount()));
        }

        if (nameFilter.test("jvm_classes_unloaded_total")) {
            sampleFamilies.add(new CounterMetricFamily("jvm_classes_unloaded_total",
                    "The total number of classes that have been unloaded since the JVM has started execution",
                    (double) this.clBean.getUnloadedClassCount()));
        }

    }

    public List<Collector.MetricFamilySamples> collect() {
        return this.collect((Predicate<String>) null);
    }

    public List<Collector.MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<Collector.MetricFamilySamples> mfs = new ArrayList<>();
        this.addClassLoadingMetrics(mfs, nameFilter == null ? SampleNameFilter.ALLOW_ALL : nameFilter);
        return mfs;
    }
}
