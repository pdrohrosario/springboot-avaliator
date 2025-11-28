package com.project.catalogservice.infrastruct.input.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.project.catalogservice.domain.Product;

public record ProductResponse(Long id, String name, BigDecimal price, String description, String category, String status, LocalDate createdAt) {
    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory().name(),
                product.getStatus().name(),
                product.getCreatedAt()
        );
    }

}
