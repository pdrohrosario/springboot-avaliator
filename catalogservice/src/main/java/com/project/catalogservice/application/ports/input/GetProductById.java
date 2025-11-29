package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.infrastruct.input.response.ProductResponse;

public interface GetProductById {
    ProductResponse execute(Long id);
}
