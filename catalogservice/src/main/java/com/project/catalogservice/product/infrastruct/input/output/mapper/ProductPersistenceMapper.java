package com.project.catalogservice.product.infrastruct.input.output.mapper;

import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.infrastruct.input.output.entities.JpaProduct;

public class ProductPersistenceMapper {

    public static Product toDomain(JpaProduct jpa) {
        return Product.fromEntity(ProductId.fromString(jpa.getId().toString()), jpa.getName(), jpa.getPrice(), jpa.getDescription(),
                jpa.getCategory().name(), jpa.getStatus().name(), jpa.getCreatedAt());
    }

    public static JpaProduct toJpa(Product product) {
        return new JpaProduct(
                product.getId().getValue(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getStatus(),
                product.getCreatedAt()
        );
    }
}
