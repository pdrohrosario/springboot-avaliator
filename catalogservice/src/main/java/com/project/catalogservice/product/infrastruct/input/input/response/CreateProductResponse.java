package com.project.catalogservice.product.infrastruct.input.input.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductResponse(String id, String name, BigDecimal price, String description, String category,
                                    String status, LocalDate createdAt) {

}
