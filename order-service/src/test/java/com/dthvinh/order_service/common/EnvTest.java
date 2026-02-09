package com.dthvinh.order_service.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class EnvTest {
    @Test
    public void testGetBootstrapServer() {
        assertEquals(Env.KAFKA_BOOTSTRAP_SERVER, System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
    }

    @Test
    public void testConstructor() {
        Env env = new Env();
        assertNotNull(env);
    }
}
