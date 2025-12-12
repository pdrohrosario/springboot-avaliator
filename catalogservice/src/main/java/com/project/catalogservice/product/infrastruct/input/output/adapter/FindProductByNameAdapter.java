package com.project.catalogservice.product.infrastruct.input.output.adapter;

import com.project.catalogservice.product.application.ports.output.FindProductByName;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductRepository;
import com.project.catalogservice.product.infrastruct.input.output.repository.JpaProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindProductByNameAdapter implements FindProductByName {

    private final ProductRepository repository;

    public FindProductByNameAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> execute(String name) {
        return repository.findByName(name);
    }
}
