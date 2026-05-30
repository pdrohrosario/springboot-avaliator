package com.project.metricservice.metrics.application.ports.output;

import java.util.UUID;

public interface ExistsProcessedReviewEvent {
    boolean exists(UUID reviewId);
}
