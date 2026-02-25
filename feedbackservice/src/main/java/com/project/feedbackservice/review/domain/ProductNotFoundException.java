package com.project.feedbackservice.review.domain;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super(String.format("Not found a Product with id: %s", productId));
    }
}
