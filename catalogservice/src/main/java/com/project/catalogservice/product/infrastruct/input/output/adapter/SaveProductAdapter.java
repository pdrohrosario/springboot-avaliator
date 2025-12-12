package com.project.catalogservice.product.infrastruct.input.output.adapter;

import com.project.catalogservice.product.application.ports.output.SaveProduct;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class SaveProductAdapter implements SaveProduct {

    private final ProductRepository repository;

    public SaveProductAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product execute(Product product) {
        return repository.save(product);
    }
}
