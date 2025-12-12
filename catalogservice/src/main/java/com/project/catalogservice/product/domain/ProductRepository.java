package com.project.catalogservice.product.domain;

import com.project.catalogservice.product.common.output.PaginatedResponse;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    Optional<Product> findByName(String name);
    PaginatedResponse<Product> findProductsByNameAndDescription(String name, String description, int page, int size);
}
