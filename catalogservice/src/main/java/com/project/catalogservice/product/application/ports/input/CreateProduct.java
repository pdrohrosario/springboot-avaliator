package com.project.catalogservice.product.application.ports.input;


import com.project.catalogservice.product.application.input.CreateProductInput;
import com.project.catalogservice.product.application.output.CreateProductOutput;

public interface CreateProduct {
    CreateProductOutput execute(CreateProductInput input);
}
