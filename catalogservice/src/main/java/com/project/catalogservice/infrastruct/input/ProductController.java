package com.project.catalogservice.infrastruct.input;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.domain.validators.ValidateCreate;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.infrastruct.input.request.ProductRequest;
import com.project.catalogservice.infrastruct.input.response.ProductResponse;


@RestController
@RequestMapping("/product")
public class ProductController {

    private final CreateProduct createProduct;

    private final GetProductById getProductById;

    private final GetProductsByNameAndDescription getProductsByNameAndDescription;

    public ProductController(CreateProduct createProduct, GetProductById getProductById, GetProductsByNameAndDescription getProductsByNameAndDescription) {
        this.createProduct = createProduct;
        this.getProductById = getProductById;
        this.getProductsByNameAndDescription = getProductsByNameAndDescription;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        return new ResponseEntity<>(createProduct.execute(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable @Valid @NotNull(message = "ProductID is required") Long id) {
        return new ResponseEntity<>(getProductById.execute(id), HttpStatus.OK);
    }

    @GetMapping("/get-products")
    public ResponseEntity<PaginatedResponse<ProductResponse>> getByNameAndDescription(
            @RequestParam(name = "name")
            String name,
            @RequestParam(name = "description")
            String description, @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable) {
        PaginatedResponse<ProductResponse> response = getProductsByNameAndDescription.execute(name, description, pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}