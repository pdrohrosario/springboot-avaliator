package com.project.feedbackservice.review.domain;

public class ProductIdIsNotValidException extends RuntimeException {
    public ProductIdIsNotValidException(String productId) {
        super(String.format("Invalid Product ID: %s", productId));
    }
}
