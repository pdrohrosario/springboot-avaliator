package com.project.catalogservice.infrastruct.product.input;

import com.project.catalogservice.application.ports.input.GetProductById;
import com.project.catalogservice.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.application.ports.output.CreateProductOutput;
import com.project.catalogservice.application.ports.output.GetProductOutput;
import com.project.catalogservice.infrastruct.product.input.mapper.ProductControllerMapper;
import com.project.catalogservice.infrastruct.product.input.response.CreateProductResponse;
import com.project.catalogservice.infrastruct.product.input.response.GetProductResponse;
import com.project.catalogservice.infrastruct.product.input.response.PaginatedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.catalogservice.application.ports.input.CreateProduct;
import com.project.catalogservice.infrastruct.product.input.request.CreateProductRequest;


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
    public ResponseEntity<CreateProductResponse> create(@RequestBody CreateProductRequest request) {
        CreateProductOutput output = createProduct.execute(ProductControllerMapper.toInput(request));
        return new ResponseEntity<>(ProductControllerMapper.toResponse(output), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductResponse> getById(@PathVariable @Valid @NotNull(message = "ProductID is required") Long id) {
        GetProductOutput output = getProductById.execute(id);
        return new ResponseEntity<>(ProductControllerMapper.toResponse(output), HttpStatus.OK);
    }

    @GetMapping("/get-products")
    public ResponseEntity<PaginatedResponse<GetProductResponse>> getByNameAndDescription(
            @RequestParam(name = "name")
            String name,
            @RequestParam(name = "description")
            String description, @PageableDefault(size = 10, page = 0, sort = "name") Pageable pageable) {

        Page<GetProductOutput> response = getProductsByNameAndDescription.execute(name, description, pageable);
        PaginatedResponse<GetProductResponse> paginatedResponse = new PaginatedResponse<>(
                response.getContent().stream().map(ProductControllerMapper::toResponse).toList(),
                response.getPageable().getPageNumber(),
                response.hasNext()
        );
        return new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
    }
}