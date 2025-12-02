package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class GetProductsByUsernameAndDescriptionUseCase implements GetProductsByNameAndDescription {

    private final FindProductsByNameAndDescription findProducts;

    public GetProductsByUsernameAndDescriptionUseCase(FindProductsByNameAndDescription findProducts) {
        this.findProducts = findProducts;
    }

    @Override
    public PaginatedResponse<ProductResponse> execute(String name, String Description, Pageable pageable) {

        if (pageable == null) {
            pageable = createPageable();
        }

        return findProducts.execute(name, Description, pageable);
    }

    private Pageable createPageable() {
        return PageRequest.of(0, 10, Sort.by("name").ascending());
    }
}
