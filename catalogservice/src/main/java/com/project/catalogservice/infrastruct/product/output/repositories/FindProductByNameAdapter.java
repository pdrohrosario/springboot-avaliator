package com.project.catalogservice.infrastruct.product.input.output.repositories;

import com.project.catalogservice.application.ports.output.FindProductByName;
import com.project.catalogservice.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class FindProductByNameAdapter implements FindProductByName {

    private final ProductRepository repository;

    public FindProductByNameAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product execute(String name) {
        return repository.findByName(name)
                .map(jpa -> Product.fromEntity(jpa.getId(), jpa.getName(), jpa.getPrice(), jpa.getDescription(),
                        jpa.getCategory().name(), jpa.getStatus().name(), jpa.getCreatedAt()))
                .orElse(null);
    }
}
