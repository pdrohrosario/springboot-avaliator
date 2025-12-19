package com.project.catalogservice.product.common.output;

import java.util.List;

public record PaginatedResponse<T> (
        List<T> items,
        int currentPage,
        boolean hasNextPage) {
}
