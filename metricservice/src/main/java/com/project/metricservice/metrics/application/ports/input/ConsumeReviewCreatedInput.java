package com.project.metricservice.metrics.application.ports.input;

import java.util.UUID;

public record ConsumeReviewCreatedInput(
    UUID reviewId,
    UUID productId,
    int rating
) {
}
