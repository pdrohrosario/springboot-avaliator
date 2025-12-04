package com.project.catalogservice.application.ports.input;

import java.math.BigDecimal;

public record CreateProductInput(String name,
                                 BigDecimal price,
                                 String description,
                                 String category) {
}
