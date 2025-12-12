package com.project.catalogservice.product.application.useCases;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.product.application.ports.output.FindProductsByNameAndDescription;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.domain.Product;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class GetProductsByUsernameAndDescriptionUseCase implements GetProductsByNameAndDescription {

    private final FindProductsByNameAndDescription findProducts;

    public GetProductsByUsernameAndDescriptionUseCase(FindProductsByNameAndDescription findProducts) {
        this.findProducts = findProducts;
    }

    @Override
    public PaginatedResponse<GetProductOutput> execute(GetProductsByNameAndDescriptionInput input) {
        PaginatedResponse<Product> products = findProducts.execute(input);

        return new PaginatedResponse<>(
                products.items().stream()
                        .map(ProductUseCaseMapper::toGetOutput)
                        .toList(),
                products.currentPage(),
                products.hasNextPage()
        );
    }
}
