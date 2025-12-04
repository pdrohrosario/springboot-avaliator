package com.project.catalogservice.infrastruct.product.input.output.repositories;

import com.project.catalogservice.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.infrastruct.product.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.product.input.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class FindProductsByNameAndDescriptionAdapter implements FindProductsByNameAndDescription {

    private final ProductRepository repository;

    public FindProductsByNameAndDescriptionAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaginatedResponse<ProductResponse> execute(String name, String description, Pageable pageable) {
        Page<ProductResponse> productsPage = repository.findByNameAndDescription(name, description, pageable)
                .map(ProductResponse::fromJpa);

        return new PaginatedResponse<>(productsPage.getContent(), productsPage.getPageable().getPageNumber(), productsPage.hasNext());
    }
}
