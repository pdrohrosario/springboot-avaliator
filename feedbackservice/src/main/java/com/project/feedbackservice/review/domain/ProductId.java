package com.project.feedbackservice.review.domain;

import com.project.feedbackservice.review.common.domain.ValueObject;

import java.util.UUID;

public class ProductId extends ValueObject {

    private final UUID value;

    private ProductId(UUID value) {
        this.value = value;
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId fromString(String value) {
        return new ProductId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
