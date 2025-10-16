package com.project.catalogservice.infrastruct.input.response;

import java.math.BigDecimal;

public record ProductRequest(Long id, String name, BigDecimal price, String description, String category) {
}
