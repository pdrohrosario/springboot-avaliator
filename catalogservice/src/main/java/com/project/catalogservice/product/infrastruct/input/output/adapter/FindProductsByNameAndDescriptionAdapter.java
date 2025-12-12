package com.project.catalogservice.product.infrastruct.input.output.adapter;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.infrastruct.input.output.mapper.ProductPersistenceMapper;
import com.project.catalogservice.product.infrastruct.input.output.repository.JpaProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class FindProductsByNameAndDescriptionAdapter implements FindProductsByNameAndDescription {

    private final JpaProductRepository repository;

    public FindProductsByNameAndDescriptionAdapter(JpaProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaginatedResponse<Product> execute(GetProductsByNameAndDescriptionInput input) {
        Pageable pageable = PageRequest.of(input.page(), input.size(), Sort.by(input.sortBy()));
        Page<Product> productsPage = repository.findByNameAndDescription(input.name(), input.description(), pageable)
                .map(ProductPersistenceMapper::toDomain);

        return new PaginatedResponse<>(productsPage.getContent(), productsPage.getPageable().getPageNumber(), productsPage.hasNext());
    }
}
