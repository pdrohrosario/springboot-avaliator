package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;

public interface GetById {
    ProductResponse execute(Long id);
}
