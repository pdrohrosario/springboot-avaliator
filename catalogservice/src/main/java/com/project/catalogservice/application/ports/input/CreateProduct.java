package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;

public interface CreateProduct {
    Product execute(ProductRequest command);
}
