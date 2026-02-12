package com.dthvinh.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        logger.info("Order Service is healthy!!!");
        return Response.ok("Order Service is healthy").build();
    }
}
