package com.project.metricservice.metrics.infrastruct.input.broker;

import java.util.UUID;
import java.time.LocalDateTime;

public record ReviewCreatedEvent(
    UUID reviewId,
    UUID productId,
    int rating,
    LocalDateTime createdAt
) {
}
