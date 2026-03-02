package com.dthvinh.rs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dthvinh.common.storage.Storage;
import com.dthvinh.inventory.api.CreateInventoryRequest;
import com.dthvinh.inventory.api.InventoryResponse;
import com.dthvinh.inventory.api.UpdateInventoryRequest;
import com.dthvinh.inventory.mapper.InventoryMapper;
import com.dthvinh.inventory.model.Inventory;

@Path("/inventories")
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {
    private final Storage<Inventory> storage;

    public InventoryResource(Storage<Inventory> storage) {
        this.storage = storage;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateInventoryRequest request) {
        if (request == null || request.getProductId() == null || request.getProductId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("productId is required").build();
        }

        Inventory inventory = new Inventory(request.getProductId(), request.getAvailable(), request.getReserved());
        storage.save(inventory.getProductId(), inventory);

        return Response.ok(InventoryMapper.toResponse(inventory)).build();
    }

    @GET
    public Response list(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        List<Inventory> inventories = storage.getAll(null, Integer.max(offset, 0), Integer.max(limit, 1));
        List<InventoryResponse> responses = inventories.stream().map(InventoryMapper::toResponse).toList();
        return Response.ok(responses).build();
    }

    @GET
    @Path("/details/{productId}")
    public Response details(@PathParam("productId") String productId) {
        Optional<Inventory> inventory = storage.get(productId);
        if (inventory.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(InventoryMapper.toResponse(inventory.get())).build();
    }

    @PUT
    @Path("/{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("productId") String productId, UpdateInventoryRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body is required").build();
        }

        Inventory inventory = new Inventory(productId, request.getAvailable(), request.getReserved());
        storage.save(productId, inventory);

        return Response.ok(InventoryMapper.toResponse(inventory)).build();
    }

    @DELETE
    @Path("/{productId}")
    public Response delete(@PathParam("productId") String productId) {
        Optional<Inventory> inventory = storage.get(productId);
        if (inventory.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        storage.delete(productId);
        return Response.ok().build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        if (storage.isHealthy()) {
            return Response
                    .ok(Map.of("status", "Inventory Service is healthy"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response
                .status(400)
                .entity(Map.of("status", "Dependencies are not healthy yet"))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
