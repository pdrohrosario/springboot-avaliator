package com.project.catalogservice.infrastruct.product.input.response;

import java.util.List;

public record PaginatedResponse<T> (
        List<T> items,
        int currentPage,
        boolean hasNextPage) {
}
