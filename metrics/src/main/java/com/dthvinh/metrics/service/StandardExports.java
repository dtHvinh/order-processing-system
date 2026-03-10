package com.dthvinh.metrics.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StandardExports extends Collector {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final StatusReader statusReader;
    private final OperatingSystemMXBean osBean;
    private final RuntimeMXBean runtimeBean;
    private final boolean linux;

    public StandardExports() {
        this(new StatusReader(), ManagementFactory.getOperatingSystemMXBean(), ManagementFactory.getRuntimeMXBean());
    }

    StandardExports(StatusReader statusReader, OperatingSystemMXBean osBean, RuntimeMXBean runtimeBean) {
        this.statusReader = statusReader;
        this.osBean = osBean;
        this.runtimeBean = runtimeBean;
        this.linux = osBean.getName().indexOf("Linux") == 0;
    }

    public List<Collector.MetricFamilySamples> collect() {
        List<Collector.MetricFamilySamples> mfs = new ArrayList();

        try {
            Long processCpuTime = callLongGetter((String) "getProcessCpuTime", this.osBean);
            mfs.add(new CounterMetricFamily("process_cpu_seconds_total",
                    "Total user and system CPU time spent in seconds.", (double) processCpuTime / (double) 1.0E9F));
        } catch (Exception e) {
            LOGGER.error("Could not access process cpu time", e);
        }

        mfs.add(new GaugeMetricFamily("process_start_time_seconds",
                "Start time of the process since unix epoch in seconds.",
                (double) this.runtimeBean.getStartTime() / (double) 1000.0F));

        try {
            Long openFdCount = callLongGetter((String) "getOpenFileDescriptorCount", this.osBean);
            mfs.add(new GaugeMetricFamily("process_open_fds", "Number of open file descriptors.",
                    (double) openFdCount));
            Long maxFdCount = callLongGetter((String) "getMaxFileDescriptorCount", this.osBean);
            mfs.add(new GaugeMetricFamily("process_max_fds", "Maximum number of open file descriptors.",
                    (double) maxFdCount));
        } catch (Exception var5) {
        }

        if (this.linux) {
            try {
                this.collectMemoryMetricsLinux(mfs);
            } catch (Exception e) {
                LOGGER.warn(e.toString());
            }
        }

        return mfs;
    }

    static Long callLongGetter(String getterName, Object obj) throws NoSuchMethodException, InvocationTargetException {
        return callLongGetter(obj.getClass().getMethod(getterName), obj);
    }

    static Long callLongGetter(Method method, Object obj) throws InvocationTargetException {
        try {
            return (Long) method.invoke(obj);
        } catch (IllegalAccessException var9) {
            for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
                try {
                    Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
                    Long result = callLongGetter(interfaceMethod, obj);
                    if (result != null) {
                        return result;
                    }
                } catch (NoSuchMethodException var8) {
                }
            }

            return null;
        }
    }

    void collectMemoryMetricsLinux(List<Collector.MetricFamilySamples> mfs) {
        BufferedReader br = null;

        try {
            br = this.statusReader.procSelfStatusReader();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("VmSize:")) {
                    mfs.add(new GaugeMetricFamily("process_virtual_memory_bytes", "Virtual memory size in bytes.",
                            (double) Float.parseFloat(line.split("\\s+")[1]) * (double) 1024.0F));
                } else if (line.startsWith("VmRSS:")) {
                    mfs.add(new GaugeMetricFamily("process_resident_memory_bytes", "Resident memory size in bytes.",
                            (double) Float.parseFloat(line.split("\\s+")[1]) * (double) 1024.0F));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOGGER.error(e.toString());
                }
            }

        }

    }

    static class StatusReader {
        StatusReader() {
        }

        BufferedReader procSelfStatusReader() throws FileNotFoundException {
            return new BufferedReader(new FileReader("/proc/self/status"));
        }
    }
}
