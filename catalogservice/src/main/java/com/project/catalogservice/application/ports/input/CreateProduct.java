package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;

public interface CreateProduct {
    ProductResponse execute(ProductRequest request);
}
