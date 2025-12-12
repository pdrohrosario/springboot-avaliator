package com.project.catalogservice.product.infrastruct.input.input.response;

import java.math.BigDecimal;

public record UpdateProductResponse(Long id, String name, BigDecimal price, String description, String category,
                              String status) {

}
