package com.project.catalogservice.product.domain;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String productName) {
        super(String.format("Already exists a product saved with name %s.", productName));
    }
}
