package com.dthvinh.order_service.common;

public final class Env {
    public final static String KAFKA_BOOTSTRAP_SERVER = System.getenv("KAFKA_BOOTSTRAP_SERVER");

    public Env() {
    }
}
