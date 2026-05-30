package com.project.metricservice.metrics.application.ports.input;

import com.project.metricservice.metrics.application.output.GetProductMetricsOutput;

import java.util.UUID;

public interface GetProductMetrics {
    GetProductMetricsOutput getMetrics(UUID productId);
}
