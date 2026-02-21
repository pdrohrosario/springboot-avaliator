package com.project.feedbackservice.review.common.output;

import java.util.List;

public record PaginatedResponse<T> (
        List<T> items,
        int currentPage,
        boolean hasNextPage) {
}
