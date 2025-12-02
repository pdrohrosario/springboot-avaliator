package com.project.catalogservice.infrastruct.input.request;

import java.math.BigDecimal;

public record ProductFilterRequest(String name,
                                   String description,
                                   String category,
                                   String status) {
}
