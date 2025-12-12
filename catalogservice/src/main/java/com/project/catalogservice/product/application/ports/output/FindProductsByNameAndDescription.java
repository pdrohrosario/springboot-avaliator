package com.project.catalogservice.product.application.ports.output;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.Product;
import org.springframework.data.domain.Pageable;


public interface FindProductsByNameAndDescription {
    PaginatedResponse<Product> execute(GetProductsByNameAndDescriptionInput filter);
}
