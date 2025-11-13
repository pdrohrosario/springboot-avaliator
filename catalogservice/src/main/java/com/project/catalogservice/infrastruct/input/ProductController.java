package com.project.catalogservice.infrastruct.input;

import com.project.catalogservice.domain.validators.ValidateCreate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;


@RestController
@RequestMapping("/product")
public class ProductController {

    private final CreateProduct createProduct;

    public ProductController(CreateProduct createProduct) {
        this.createProduct = createProduct;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        Product createdProduct = createProduct.execute(request);
        return new ResponseEntity<>(ProductResponse.fromDomain(createdProduct), HttpStatus.CREATED);
    }
}