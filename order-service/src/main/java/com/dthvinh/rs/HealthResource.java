package com.dthvinh.rs;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dthvinh.common.storage.Storage;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    private final Storage<?> storage;

    public HealthResource(Storage<?> storage) {
        this.storage = storage;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello() {
        if (storage.isHealthy()) {
            return Response
                    .ok(Map.of("status", "Order Service is healthy"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response
                .status(400)
                .entity(Map.of("status", "Redis has not connected yet"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
