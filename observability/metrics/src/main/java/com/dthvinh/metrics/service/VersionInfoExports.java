package com.dthvinh.metrics.service;

import java.util.List;

import io.prometheus.client.Collector;
import io.prometheus.client.Info;

public class VersionInfoExports extends Collector {
    public VersionInfoExports() {
    }

    public List<Collector.MetricFamilySamples> collect() {
        Info i = ((Info.Builder) ((Info.Builder) Info.build().name("jvm")).help("VM version info")).create();
        i.info(new String[] { "version", System.getProperty("java.runtime.version", "unknown"), "vendor",
                System.getProperty("java.vm.vendor", "unknown"), "runtime",
                System.getProperty("java.runtime.name", "unknown") });
        return i.collect();
    }
}
