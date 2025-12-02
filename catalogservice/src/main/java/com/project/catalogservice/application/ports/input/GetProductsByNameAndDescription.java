package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface GetProductsByNameAndDescription {
    PaginatedResponse<ProductResponse> execute(String name, String Description, Pageable pageable);
}
