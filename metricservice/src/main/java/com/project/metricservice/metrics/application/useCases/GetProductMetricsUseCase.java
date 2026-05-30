package com.project.metricservice.metrics.application.useCases;

import com.project.metricservice.metrics.application.output.GetProductMetricsOutput;
import com.project.metricservice.metrics.application.ports.input.GetProductMetrics;
import com.project.metricservice.metrics.application.ports.output.FindProductMetricsByProductId;
import com.project.metricservice.metrics.domain.ProductMetrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class GetProductMetricsUseCase implements GetProductMetrics {

    private final FindProductMetricsByProductId findProductMetricsByProductId;

    public GetProductMetricsUseCase(FindProductMetricsByProductId findProductMetricsByProductId) {
        this.findProductMetricsByProductId = findProductMetricsByProductId;
    }

    @Override
    public GetProductMetricsOutput getMetrics(UUID productId) {
        Optional<ProductMetrics> metricsOpt = findProductMetricsByProductId.findByProductId(productId);

        if (metricsOpt.isEmpty()) {
            Map<Integer, Long> zeroedDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                zeroedDistribution.put(i, 0L);
            }
            return new GetProductMetricsOutput(
                productId,
                0L,
                null,
                zeroedDistribution,
                true
            );
        }

        ProductMetrics metrics = metricsOpt.get();
        return new GetProductMetricsOutput(
            productId,
            metrics.getTotalReviews(),
            metrics.getAverageRating(),
            metrics.getRatingDistribution(),
            false
        );
    }
}
