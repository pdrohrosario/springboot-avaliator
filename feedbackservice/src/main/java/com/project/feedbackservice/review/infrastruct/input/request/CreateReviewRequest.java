package com.project.feedbackservice.review.infrastruct.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @NotBlank(message = "Product ID is required")
        String productId,
        @NotNull(message = "Rating is required")
        Integer rating,
        String comment) {
}
