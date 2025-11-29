package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;

public interface ListAllProducts {
    PaginatedResponse<ProductResponse> execute();
}
