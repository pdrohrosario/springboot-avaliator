package com.project.catalogservice.product.application.useCases;

import com.project.catalogservice.product.application.mapper.ProductUseCaseMapper;
import com.project.catalogservice.product.application.ports.input.CreateProduct;
import com.project.catalogservice.product.application.input.CreateProductInput;
import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.ports.output.FindProductByName;
import com.project.catalogservice.product.application.ports.output.SaveProduct;
import com.project.catalogservice.product.domain.ProductAlreadyExistsException;
import org.springframework.stereotype.Service;

import com.project.catalogservice.product.domain.Product;

@Service
public class CreateProductUseCase implements CreateProduct {

    private final SaveProduct save;

    private final FindProductByName findByName;

    public CreateProductUseCase(SaveProduct save, FindProductByName findByName) {
        this.save = save;
        this.findByName = findByName;
    }

    @Override
    public CreateProductOutput execute(CreateProductInput input) {
        findByName.execute(input.name()).ifPresent(productFound -> {
            throw new ProductAlreadyExistsException(productFound.getName());
        });
        Product newProduct = Product.create(input.name(), input.price(), input.description(), input.category());
        newProduct = save.execute(newProduct);
        return ProductUseCaseMapper.toCreateOutput(newProduct);
    }
}
