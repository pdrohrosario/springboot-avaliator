package com.project.catalogservice.infrastruct.input.response;

import java.util.List;

public record PaginatedResponse<T> (
        List<T> items,
        int currentPage,
        boolean hasNextPage) {
}
