package com.project.catalogservice.product.domain;

public class ProductNotFound extends RuntimeException {
    public ProductNotFound(String productId) {
        super(String.format("Not found a Product with id: %s", productId));
    }
}
