package com.dthvinh.order_service.common;

public final class Env {
    public final static String KAFKA_BOOTSTRAP_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS");

    public Env() {
    }
}
