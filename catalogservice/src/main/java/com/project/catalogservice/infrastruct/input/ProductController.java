package com.project.catalogservice.infrastruct.input;

import com.project.catalogservice.application.ports.input.GetById;
import com.project.catalogservice.domain.validators.ValidateCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    private final GetById getById;

    public ProductController(CreateProduct createProduct, GetById getById) {
        this.createProduct = createProduct;
        this.getById = getById;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        Product createdProduct = createProduct.execute(request);
        return new ResponseEntity<>(ProductResponse.fromDomain(createdProduct), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable @Valid @NotNull(message = "ProductID is required") Long id) {
        Product product= getById.execute(id);
        return new ResponseEntity<>(product != null ? ProductResponse.fromDomain(product) : null, HttpStatus.OK);
    }
}