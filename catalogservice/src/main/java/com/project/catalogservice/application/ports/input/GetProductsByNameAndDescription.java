package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.application.ports.output.GetProductOutput;
import com.project.catalogservice.infrastruct.product.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.product.input.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetProductsByNameAndDescription {
    Page<GetProductOutput> execute(String name, String Description, Pageable pageable);
}
