package com.project.metricservice.metrics.application.useCases;

import com.project.metricservice.metrics.application.ports.input.ConsumeReviewCreatedInput;
import com.project.metricservice.metrics.application.ports.output.LoadOrCreateProductMetrics;
import com.project.metricservice.metrics.application.ports.output.SaveProductMetrics;
import com.project.metricservice.metrics.application.ports.output.ExistsProcessedReviewEvent;
import com.project.metricservice.metrics.application.ports.output.RegisterProcessedReviewEvent;
import com.project.metricservice.metrics.domain.ProductMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConsumeReviewCreatedUseCase.
 * Covers US-05: Update product metrics from review-created event.
 * Focuses on idempotency and delegation to domain.
 */
@ExtendWith(MockitoExtension.class)
class ConsumeReviewCreatedUseCaseTest {

    @Mock
    private LoadOrCreateProductMetrics loadOrCreateProductMetrics;

    @Mock
    private SaveProductMetrics saveProductMetrics;

    @Mock
    private ExistsProcessedReviewEvent existsProcessedReviewEvent;

    @Mock
    private RegisterProcessedReviewEvent registerProcessedReviewEvent;

    @InjectMocks
    private ConsumeReviewCreatedUseCase useCase;

    private UUID productId;
    private UUID reviewId;
    private ConsumeReviewCreatedInput input;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        input = new ConsumeReviewCreatedInput(reviewId, productId, 5);
    }

    @Test
    @DisplayName("Should process review created event when not already processed")
    void shouldProcessEventWhenNotProcessed() {
        // Given
        when(existsProcessedReviewEvent.exists(reviewId)).thenReturn(false);
        ProductMetrics metrics = ProductMetrics.create(productId);
        when(loadOrCreateProductMetrics.loadOrCreate(productId)).thenReturn(metrics);

        // When
        useCase.consume(input);

        // Then
        verify(existsProcessedReviewEvent).exists(reviewId);
        verify(loadOrCreateProductMetrics).loadOrCreate(productId);
        verify(saveProductMetrics).save(argThat(m -> 
            m.getProductId().equals(productId) && m.getTotalReviews() == 1
        ));
        verify(registerProcessedReviewEvent).register(reviewId);
    }

    @Test
    @DisplayName("Should skip processing when event is already processed (idempotency)")
    void shouldSkipWhenAlreadyProcessed() {
        // Given
        when(existsProcessedReviewEvent.exists(reviewId)).thenReturn(true);

        // When
        useCase.consume(input);

        // Then
        verify(existsProcessedReviewEvent).exists(reviewId);
        
        // Ensure no metrics update occurs
        verify(loadOrCreateProductMetrics, never()).loadOrCreate(any());
        verify(saveProductMetrics, never()).save(any());
        verify(registerProcessedReviewEvent, never()).register(any());
    }

    @Test
    @DisplayName("Should handle domain exceptions during processing")
    void shouldHandleDomainExceptions() {
        // Given
        when(existsProcessedReviewEvent.exists(reviewId)).thenReturn(false);
        when(loadOrCreateProductMetrics.loadOrCreate(productId)).thenThrow(new RuntimeException("DB Error"));

        // When & Then
        try {
            useCase.consume(input);
        } catch (Exception e) {
            // Expected
        }
        
        verify(saveProductMetrics, never()).save(any());
        verify(registerProcessedReviewEvent, never()).register(any());
    }
}
