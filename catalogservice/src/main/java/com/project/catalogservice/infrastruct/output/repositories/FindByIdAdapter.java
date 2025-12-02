package com.project.catalogservice.infrastruct.output.repositories;

import com.project.catalogservice.application.ports.output.FindById;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class FindByIdAdapter implements FindById {

    private final ProductRepository repository;

    public FindByIdAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductResponse execute(Long id) {
        return repository.findById(id)
                .map(ProductResponse::fromJpa
                ).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
}
