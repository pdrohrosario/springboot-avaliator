package com.project.metricservice.metrics.application.ports.output;

import java.util.UUID;

public interface RegisterProcessedReviewEvent {
    void register(UUID reviewId);
}
