package com.dthvinh.rs;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response hello() {
        return Response
                .ok(Map.of("status", "Order Service is healthy"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
