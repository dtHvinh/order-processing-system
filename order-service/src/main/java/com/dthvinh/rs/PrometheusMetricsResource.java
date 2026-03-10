package com.dthvinh.rs;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.dthvinh.metrics.service.DefaultExports;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;

@Path("/prometheus/metrics")
public class PrometheusMetricsResource {

    private static final Gauge serviceUp = Gauge.build()
            .name("order_service_up")
            .help("1 if the order-service metrics endpoint is reachable")
            .create();

    static {
        DefaultExports.initialize();
        registerCollectorSafely(serviceUp);
        serviceUp.set(1);
    }

    private static void registerCollectorSafely(io.prometheus.client.Collector collector) {
        try {
            CollectorRegistry.defaultRegistry.register(collector);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @GET
    @Produces("text/plain; version=0.0.4; charset=utf-8")
    public Response metrics() throws IOException {
        StringWriter writer = new StringWriter();
        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        return Response.ok(writer.toString())
                .type(TextFormat.CONTENT_TYPE_004)
                .build();
    }
}