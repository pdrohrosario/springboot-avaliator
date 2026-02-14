package com.project.feedbackservice.review.infrastruct.input.response;

import java.time.LocalDate;

public record CreateReviewResponse (String reviewId, String productId, Integer rating, String comment, LocalDate createdAt) {
}
