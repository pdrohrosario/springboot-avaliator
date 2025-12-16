package com.project.catalogservice.product.application.useCases;

import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.input.GetProductById;
import com.project.catalogservice.product.application.ports.output.FindById;
import com.project.catalogservice.product.domain.Product;
import com.project.catalogservice.product.domain.ProductId;
import com.project.catalogservice.product.domain.ProductNotFound;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetProductByIdUseCase implements GetProductById {

    private final FindById find;

    public GetProductByIdUseCase(FindById find) {
        this.find = find;
    }

    @Override
    public GetProductOutput execute(String id) {
        ProductId productId = ProductId.fromString(id);
        Optional<Product> productSearched = find.execute(productId);
        if (productSearched.isEmpty()) {
            throw new ProductNotFound(productId.getValue());
        }
        return ProductUseCaseMapper.toGetOutput(productSearched.get());

    }
}
