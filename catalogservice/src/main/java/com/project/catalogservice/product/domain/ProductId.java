package com.project.catalogservice.product.domain;

import com.project.catalogservice.product.common.domain.ValueObject;

import java.util.UUID;

public class ProductId extends ValueObject {

    private final String value;

    public ProductId(String value) {
        this.value = value;
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId fromString(String value) {
        return new ProductId(value);
    }

    public String getValue() {
        return value;
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
