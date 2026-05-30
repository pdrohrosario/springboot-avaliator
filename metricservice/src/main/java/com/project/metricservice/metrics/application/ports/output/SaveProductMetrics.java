package com.project.metricservice.metrics.application.ports.output;

import com.project.metricservice.metrics.domain.ProductMetrics;

public interface SaveProductMetrics {
    void save(ProductMetrics productMetrics);
}
