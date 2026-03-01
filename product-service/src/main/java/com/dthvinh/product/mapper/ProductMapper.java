package com.dthvinh.product.mapper;

import com.dthvinh.product.api.ProductResponse;
import com.dthvinh.product.model.Product;

public class ProductMapper {
    public static ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse(product.getProductId(), product.getQuantity(),
                product.getUnitPrice());
        return response;
    }
}
