package com.project.feedbackservice.review.infrastruct.output.broker;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewCreatedEvent(
    UUID reviewId,
    UUID productId,
    int rating,
    LocalDateTime createdAt
) {
}
