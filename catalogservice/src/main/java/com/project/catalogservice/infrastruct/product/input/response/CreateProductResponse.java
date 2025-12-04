package com.project.catalogservice.infrastruct.product.input.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductResponse(Long id, String name, BigDecimal price, String description, String category,
                                    String status, LocalDate createdAt) {

}
