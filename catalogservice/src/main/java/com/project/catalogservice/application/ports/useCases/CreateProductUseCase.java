package com.project.catalogservice.application.ports.useCases;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.domain.Product;

public class CreateProductUseCase implements CreateProduct {


    @Override
    public Product execute(Product newProduct) {
        return newProduct;
    }
}
