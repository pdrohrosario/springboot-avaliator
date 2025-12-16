package com.project.catalogservice.product.infrastruct.input.input;

import com.project.catalogservice.product.application.input.GetProductsByNameAndDescriptionInput;
import com.project.catalogservice.product.application.ports.input.CreateProduct;
import com.project.catalogservice.product.application.ports.input.GetProductById;
import com.project.catalogservice.product.application.ports.input.GetProductsByNameAndDescription;
import com.project.catalogservice.product.application.output.CreateProductOutput;
import com.project.catalogservice.product.application.output.GetProductOutput;
import com.project.catalogservice.product.common.output.PaginatedResponse;
import com.project.catalogservice.product.infrastruct.input.input.mapper.ProductControllerMapper;
import com.project.catalogservice.product.infrastruct.input.input.response.CreateProductResponse;
import com.project.catalogservice.product.infrastruct.input.input.response.GetProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.project.catalogservice.product.infrastruct.input.input.request.CreateProductRequest;

import java.util.Collection;
import java.util.List;


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
    public ResponseEntity<GetProductResponse> getById(@PathVariable @Valid @NotNull(message = "ProductID is required") String id) {
        GetProductOutput output = getProductById.execute(id);
        return new ResponseEntity<>(ProductControllerMapper.toResponse(output), HttpStatus.OK);
    }

    @GetMapping("/get-products")
    public ResponseEntity<PaginatedResponse<GetProductResponse>> getByNameAndDescription(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "dueDate") String sort)  {

        GetProductsByNameAndDescriptionInput input = new GetProductsByNameAndDescriptionInput(name, description, page, size, sort);
        PaginatedResponse<GetProductOutput> response = getProductsByNameAndDescription.execute(input);
        PaginatedResponse<GetProductResponse> paginatedResponse = new PaginatedResponse<>(
                response.items() == null ? List.of() : response.items().stream().map(ProductControllerMapper::toResponse).toList(),
                response.currentPage(),
                response.hasNextPage()
        );
        return new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
    }
}