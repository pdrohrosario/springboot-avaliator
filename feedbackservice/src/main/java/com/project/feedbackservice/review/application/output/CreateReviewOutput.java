package com.project.feedbackservice.review.application.output;

import java.time.LocalDate;

public record CreateReviewOutput(String reviewId, String productId, Integer rating, String comment, LocalDate createdAt) {
}
