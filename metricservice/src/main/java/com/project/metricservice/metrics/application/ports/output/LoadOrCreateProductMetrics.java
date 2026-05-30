package com.project.metricservice.metrics.application.ports.output;

import com.project.metricservice.metrics.domain.ProductMetrics;

import java.util.UUID;

public interface LoadOrCreateProductMetrics {
    ProductMetrics loadOrCreate(UUID productId);
}
