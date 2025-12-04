package com.project.catalogservice.infrastruct.product.input.mapper;

import com.project.catalogservice.application.ports.input.CreateProductInput;
import com.project.catalogservice.application.ports.output.CreateProductOutput;
import com.project.catalogservice.application.ports.output.GetProductOutput;
import com.project.catalogservice.infrastruct.product.input.request.CreateProductRequest;
import com.project.catalogservice.infrastruct.product.input.response.CreateProductResponse;
import com.project.catalogservice.infrastruct.product.input.response.GetProductResponse;

public class ProductControllerMapper {

    public static CreateProductInput toInput(CreateProductRequest request) {
        return new CreateProductInput(
            request.productName(),
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
