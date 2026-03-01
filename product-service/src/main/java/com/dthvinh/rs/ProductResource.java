package com.dthvinh.rs;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.dthvinh.product.api.CreateProductRequest;
import com.dthvinh.product.api.ProductResponse;
import com.dthvinh.product.api.UpdateProductRequest;
import com.dthvinh.product.mapper.ProductMapper;
import com.dthvinh.product.model.Product;
import com.dthvinh.service.storage.Storage;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {
    private final Storage<Product> storage;

    public ProductResource(Storage<Product> storage) {
        this.storage = storage;
    }

    @GET
    @Path("/details/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductDetails(@PathParam("productId") String productId) {
        Optional<Product> product = storage.get(productId);
        if (product.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ProductResponse response = ProductMapper.toProductResponse(product.get());
        return Response.ok(response).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductList(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        List<Product> products = storage.getAll(null, offset, limit);
        List<ProductResponse> responses = products.stream().map(ProductMapper::toProductResponse).toList();

        return Response.ok(responses).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(CreateProductRequest request) {
        Product product = new Product(request.getProductId(), request.getQuantity(), request.getUnitPrice());
        storage.save(product.getProductId(), product);

        return Response.ok(request).build();
    }

    @PUT
    @Path("/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("productId") String productId, UpdateProductRequest request) {
        Product product = new Product(productId, request.getQuantity(), request.getUnitPrice());
        storage.save(product.getProductId(), product);

        return Response.ok(request).build();
    }

    @DELETE
    @Path("/{productId}")
    public Response deleteProduct(@PathParam("productId") String productId) {
        Optional<Product> product = storage.get(productId);
        if (product.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        storage.delete(productId);

        return Response.ok().build();
    }
}
