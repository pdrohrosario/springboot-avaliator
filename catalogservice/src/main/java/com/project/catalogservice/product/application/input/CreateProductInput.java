package com.project.catalogservice.product.application.input;

import java.math.BigDecimal;

public record CreateProductInput(String name,
                                 BigDecimal price,
                                 String description,
                                 String category) {
}
