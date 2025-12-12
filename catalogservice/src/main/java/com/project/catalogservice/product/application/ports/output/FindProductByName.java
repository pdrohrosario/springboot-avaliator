package com.project.catalogservice.product.application.ports.output;

import com.project.catalogservice.product.domain.Product;

import java.util.Optional;

public interface FindProductByName {
    Optional<Product> execute(String name);
}
