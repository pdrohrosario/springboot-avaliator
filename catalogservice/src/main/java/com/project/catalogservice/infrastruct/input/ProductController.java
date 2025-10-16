package com.project.catalogservice.infrastruct.input;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.application.ports.useCases.CreateProductUseCase;
import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.input.response.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;


@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProduct createProduct;

    @Autowired
    public ProductController(CreateProduct createProduct) {
        this.createProduct = createProduct;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        Product createdProduct = createProduct.execute(Product.create(request.id(), request.name(), request.price(), request.description(), request.category()));
        return new ResponseEntity<>(ProductResponse.fromDomain(createdProduct), HttpStatus.CREATED);
    }
}