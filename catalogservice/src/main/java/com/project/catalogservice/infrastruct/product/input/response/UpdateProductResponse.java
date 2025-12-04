package com.project.catalogservice.infrastruct.product.input.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.project.catalogservice.domain.Product;
import com.project.catalogservice.infrastruct.product.input.output.entities.JpaProduct;

public record UpdateProductResponse(Long id, String name, BigDecimal price, String description, String category,
                              String status) {

}
