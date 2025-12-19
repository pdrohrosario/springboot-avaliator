package com.project.catalogservice.product.application.output;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductOutput (String id, String name, BigDecimal price, String description, String category,
                                   String status, LocalDate createdAt){
}
