package com.project.catalogservice.product.application.useCases;

import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.application.ports.input.GetProductById;
import com.project.catalogservice.product.application.ports.output.FindById;
import com.project.catalogservice.product.domain.ProductId;
import org.springframework.stereotype.Service;

@Service
public class GetProductByIdUseCase implements GetProductById {

    private final FindById find;

    public GetProductByIdUseCase(FindById find) {
        this.find = find;
    }

    @Override
    public GetProductOutput execute(String id) {
        ProductId productId = ProductId.fromString(id);
        return ProductUseCaseMapper.toGetOutput(find.execute(productId).orElseThrow(() -> new RuntimeException("Not found a Product with id: " + id)));
    }
}
