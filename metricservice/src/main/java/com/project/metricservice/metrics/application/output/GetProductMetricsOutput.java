package com.project.metricservice.metrics.application.output;

import java.util.Map;
import java.util.UUID;

public record GetProductMetricsOutput(
    UUID productId,
    long totalReviews,
    Double averageRating,
    Map<Integer, Long> ratingDistribution,
    boolean noReviewsYet
) {
}
