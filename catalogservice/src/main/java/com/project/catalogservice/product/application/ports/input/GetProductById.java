package com.project.catalogservice.product.application.ports.input;


import com.project.catalogservice.product.application.output.GetProductOutput;

public interface GetProductById {
    GetProductOutput execute(String id);
}
