package com.project.catalogservice.infrastruct.output.repositories;

import com.project.catalogservice.application.ports.output.FindById;
import com.project.catalogservice.domain.Product;
import org.springframework.stereotype.Component;

@Component
public class FindByIdAdapter implements FindById {

    private final ProductRepository repository;

    public FindByIdAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product execute(Long id) {
        return repository.findById(id)
                .map(jpa -> Product.fromEntity(jpa.getId(), jpa.getName(), jpa.getPrice(), jpa.getDescription(),
                        jpa.getCategory().name(), jpa.getStatus().name(), jpa.getCreatedAt()))
                .orElse(null);
    }
}
