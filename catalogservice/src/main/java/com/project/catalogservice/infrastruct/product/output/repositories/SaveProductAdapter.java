package com.project.catalogservice.infrastruct.product.input.output.repositories;

import com.project.catalogservice.application.ports.output.SaveProduct;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.product.input.output.entities.JpaProduct;
import org.springframework.stereotype.Component;

@Component
public class SaveProductAdapter implements SaveProduct {

    private final ProductRepository repository;

    public SaveProductAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product execute(Product product) {
        JpaProduct jpa = new JpaProduct(product);
        jpa = repository.save(jpa);
        return Product.fromEntity(jpa.getId(), jpa.getName(), jpa.getPrice(), jpa.getDescription(),
                jpa.getCategory().name(), jpa.getStatus().name(), jpa.getCreatedAt());
    }
}
