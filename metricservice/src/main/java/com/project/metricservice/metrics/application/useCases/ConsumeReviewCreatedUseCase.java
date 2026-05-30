package com.project.metricservice.metrics.application.useCases;

import com.project.metricservice.metrics.application.ports.input.ConsumeReviewCreated;
import com.project.metricservice.metrics.application.ports.input.ConsumeReviewCreatedInput;
import com.project.metricservice.metrics.application.ports.output.ExistsProcessedReviewEvent;
import com.project.metricservice.metrics.application.ports.output.LoadOrCreateProductMetrics;
import com.project.metricservice.metrics.application.ports.output.RegisterProcessedReviewEvent;
import com.project.metricservice.metrics.application.ports.output.SaveProductMetrics;
import com.project.metricservice.metrics.domain.ProductMetrics;

import org.springframework.stereotype.Service;

@Service
public class ConsumeReviewCreatedUseCase implements ConsumeReviewCreated {

    private final LoadOrCreateProductMetrics loadOrCreateProductMetrics;
    private final SaveProductMetrics saveProductMetrics;
    private final ExistsProcessedReviewEvent existsProcessedReviewEvent;
    private final RegisterProcessedReviewEvent registerProcessedReviewEvent;

    public ConsumeReviewCreatedUseCase(
            LoadOrCreateProductMetrics loadOrCreateProductMetrics,
            SaveProductMetrics saveProductMetrics,
            ExistsProcessedReviewEvent existsProcessedReviewEvent,
            RegisterProcessedReviewEvent registerProcessedReviewEvent) {
        this.loadOrCreateProductMetrics = loadOrCreateProductMetrics;
        this.saveProductMetrics = saveProductMetrics;
        this.existsProcessedReviewEvent = existsProcessedReviewEvent;
        this.registerProcessedReviewEvent = registerProcessedReviewEvent;
    }

    @Override
    public void consume(ConsumeReviewCreatedInput input) {
        if (existsProcessedReviewEvent.exists(input.reviewId())) {
            return; // Idempotency check
        }

        ProductMetrics metrics = loadOrCreateProductMetrics.loadOrCreate(input.productId());
        metrics.addRating(input.rating());

        saveProductMetrics.save(metrics);
        registerProcessedReviewEvent.register(input.reviewId());
    }
}
