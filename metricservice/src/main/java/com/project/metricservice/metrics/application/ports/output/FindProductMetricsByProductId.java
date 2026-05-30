package com.project.metricservice.metrics.application.ports.output;

import com.project.metricservice.metrics.domain.ProductMetrics;

import java.util.Optional;
import java.util.UUID;

public interface FindProductMetricsByProductId {
    Optional<ProductMetrics> findByProductId(UUID productId);
}
