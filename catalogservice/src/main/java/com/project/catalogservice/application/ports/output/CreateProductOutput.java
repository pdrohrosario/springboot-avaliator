package com.project.catalogservice.application.ports.output;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductOutput (Long id, String name, BigDecimal price, String description, String category,
                                   String status, LocalDate createdAt){
}
