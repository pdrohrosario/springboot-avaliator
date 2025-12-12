package com.project.catalogservice.product.infrastruct.input.output.adapter;

import com.project.catalogservice.product.application.ports.output.FindById;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.domain.ProductRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindByIdAdapter implements FindById {

    private final ProductRepository repository;

    public FindByIdAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> execute(ProductId id) {
        return repository.findById(id);
    }
}
