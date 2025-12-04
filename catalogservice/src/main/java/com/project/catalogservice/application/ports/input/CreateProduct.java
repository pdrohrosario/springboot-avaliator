package com.project.catalogservice.application.ports.input;

import com.project.catalogservice.application.ports.output.CreateProductOutput;

public interface CreateProduct {
    CreateProductOutput execute(CreateProductInput input);
}
