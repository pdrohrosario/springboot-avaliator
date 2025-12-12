package com.project.catalogservice.product.infrastruct.input.input.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        @NotBlank(message = "Name is required") String productName,
        @NotNull(message = "Price is required") BigDecimal price,
        String description,
        @NotBlank(message = "Category is required") String category) {

}
