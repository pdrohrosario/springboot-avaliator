package com.project.catalogservice.product.infrastruct.input.input.mapper;

import com.project.catalogservice.product.application.input.CreateProductInput;
import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.infrastruct.input.input.request.CreateProductRequest;
import com.project.catalogservice.product.infrastruct.input.input.response.CreateProductResponse;
import com.project.catalogservice.product.infrastruct.input.input.response.GetProductResponse;

public class ProductControllerMapper {

    public static CreateProductInput toInput(CreateProductRequest request) {
        return new CreateProductInput(
            request.name(),
            request.price(),
            request.description(),
            request.category()
        );
    }

    public static CreateProductResponse toResponse(CreateProductOutput output) {
        return new CreateProductResponse(
            output.id(),
            output.name(),
            output.price(),
            output.description(),
            output.category(),
            output.status(),
            output.createdAt()
        );
    }

    public static GetProductResponse toResponse(GetProductOutput output) {
        return new GetProductResponse(
            output.id(),
            output.name(),
            output.price(),
            output.description(),
            output.category(),
            output.status(),
            output.createdAt()
        );
    }
}
