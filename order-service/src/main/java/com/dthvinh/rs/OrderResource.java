package com.dthvinh.rs;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dthvinh.common.storage.Storage;
import com.dthvinh.messaging.publisher.Publisher;
import com.dthvinh.order.api.CreateOrderRequest;
import com.dthvinh.order.api.OrderResponse;
import com.dthvinh.order.mapper.OrderMapper;
import com.dthvinh.order.model.Order;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OrderMapper mapper = new OrderMapper();
    private final Publisher publisher;
    private final Storage<Order> storage;

    public OrderResource(Publisher publisher, Storage<Order> storage) {
        this.publisher = publisher;
        this.storage = storage;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrder(CreateOrderRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body is required").build();
        }

        String orderId = "ord_" + UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();

        Order order = mapper.toOrder(request, orderId, now);
        publisher.send("ORDER", orderId, order);
        storage.save(orderId, order);

        OrderResponse response = mapper.toResponse(order);
        logger.info("Created order id={} userId={} items={} totalAmount={} currency={}",
                response.getId(), response.getUserId(),
                response.getItems() != null ? response.getItems().size() : 0,
                response.getTotalAmount(), response.getCurrency());

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public Response getOrders(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        List<Order> orders = storage.getAll(null, Integer.max(offset, 0), Integer.max(limit, 1));

        List<OrderResponse> responses = mapper.toResponses(orders);

        return Response.ok(responses).build();
    }
}
