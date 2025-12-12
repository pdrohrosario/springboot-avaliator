package com.project.catalogservice.product.infrastruct.input.output.repository;

import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.domain.ProductRepository;
import com.project.catalogservice.product.infrastruct.input.output.entities.JpaProduct;
import com.project.catalogservice.product.infrastruct.input.output.mapper.ProductPersistenceMapper;

import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository repository;

    public ProductRepositoryImpl(JpaProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product save(Product product) {
        JpaProduct jpa = ProductPersistenceMapper.toJpa(product);
        jpa = repository.save(jpa);
        return ProductPersistenceMapper.toDomain(jpa);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id.getValue()).map(ProductPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Product> findByName(String name) {
        return repository.findByName(name).map(ProductPersistenceMapper::toDomain);
    }

    @Override
    public PaginatedResponse<Product> findProductsByNameAndDescription(String name, String description, int page, int size) {
        return null;
    }
}
