package com.project.catalogservice.product.infrastruct.input.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest (
    @NotNull (message = "Product ID is required") Long productId,
    @NotBlank(message = "Name is required") String name,
    @NotNull(message = "Price is required") BigDecimal price,
    String description,
    @NotBlank(message = "Category is required") String category,
    @NotBlank(message = "Status is required") String status){
}
