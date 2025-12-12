package com.project.catalogservice.product.application.ports.input;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.common.output.PaginatedResponse;

public interface GetProductsByNameAndDescription {
    PaginatedResponse<GetProductOutput> execute(GetProductsByNameAndDescriptionInput input);
}
