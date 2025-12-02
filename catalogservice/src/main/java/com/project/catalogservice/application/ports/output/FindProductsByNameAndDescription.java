package com.project.catalogservice.application.ports.output;

import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.springframework.data.domain.Pageable;


public interface FindProductsByNameAndDescription {
    PaginatedResponse<ProductResponse> execute(String name, String description, Pageable pageable);
}
