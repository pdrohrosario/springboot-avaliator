package com.project.catalogservice.product.application.mapper;

import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.domain.Product;

public class ProductUseCaseMapper {

    public static CreateProductOutput toCreateOutput(Product product) {
        return new CreateProductOutput(
            product.getId().getValue(),
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getCategory().name(),
            product.getStatus().name(),
            product.getCreatedAt()
        );
    }

    public static GetProductOutput toGetOutput(Product product) {
        return new GetProductOutput(
            product.getId().getValue(),
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getCategory().name(),
            product.getStatus().name(),
            product.getCreatedAt()
        );
    }
}
