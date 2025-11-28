package com.project.catalogservice.infrastruct.input.request;

import java.math.BigDecimal;

import com.project.catalogservice.domain.validators.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(@NotNull(groups = ValidateUpdate.class, message = "ID is required") Long id,
                             @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Name is required")
                             @Size(groups = {ValidateUpdate.class, ValidateCreate.class}, min = 5, max = 50, message = "Name must be between 10 and 50 characters") String name,
                             @NotNull(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Price is required") BigDecimal price,
                             @Size(groups = {ValidateUpdate.class, ValidateCreate.class}, min = 5, max = 200, message = "Description must be between 10 and 200 characters") String description,
                             @NotBlank(groups = {ValidateUpdate.class, ValidateCreate.class}, message = "Category is required") String category) {
}
