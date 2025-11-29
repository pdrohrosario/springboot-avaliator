package com.project.catalogservice.infrastruct.input;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.ListAllProducts;
import com.project.catalogservice.domain.validators.ValidateCreate;
import com.project.catalogservice.infrastruct.input.request.ProductFilterRequest;
import com.project.catalogservice.infrastruct.input.response.PaginatedResponse;
import jakarta.validation.Valid;
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

    private final ListAllProducts listAllProducts;

    public ProductController(CreateProduct createProduct, GetProductById getProductById, ListAllProducts listAllProducts) {
        this.createProduct = createProduct;
        this.getProductById = getProductById;
        this.listAllProducts = listAllProducts;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> create(@RequestBody @Validated(ValidateCreate.class) ProductRequest request) {
        return new ResponseEntity<>(createProduct.execute(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable @Valid @NotNull(message = "ProductID is required") Long id) {
        return new ResponseEntity<>(getProductById.execute(id), HttpStatus.OK);
    }

    @GetMapping("/search-products")
    public ResponseEntity<PaginatedResponse<ProductResponse>> searchProducts(ProductFilterRequest filter, @PageableDefault(size = 10, page = 0,sort = "name") Pageable pageable) {
        return new ResponseEntity<>(listAllProducts.execute(),HttpStatus.OK);
    }
}