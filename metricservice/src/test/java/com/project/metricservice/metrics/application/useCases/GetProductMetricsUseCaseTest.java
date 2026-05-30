package com.project.metricservice.metrics.application.useCases;

import com.project.metricservice.metrics.application.ports.output.FindProductMetricsByProductId;
import com.project.metricservice.metrics.application.output.GetProductMetricsOutput;
import com.project.metricservice.metrics.domain.ProductMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetProductMetricsUseCase.
 * Covers US-06: Query product rating metrics.
 */
@ExtendWith(MockitoExtension.class)
class GetProductMetricsUseCaseTest {

    @Mock
    private FindProductMetricsByProductId findProductMetricsByProductId;

    @InjectMocks
    private GetProductMetricsUseCase useCase;

    @Test
    @DisplayName("Should return metrics when product has reviews")
    void shouldReturnMetricsWhenProductHasReviews() {
        // Given
        UUID productId = UUID.randomUUID();
        ProductMetrics metrics = ProductMetrics.create(productId);
        metrics.addRating(5);
        when(findProductMetricsByProductId.findByProductId(productId)).thenReturn(Optional.of(metrics));

        // When
        GetProductMetricsOutput output = useCase.getMetrics(productId);

        // Then
        assertNotNull(output);
        assertEquals(productId, output.productId());
        assertEquals(1L, output.totalReviews());
        assertEquals(5.0, output.averageRating());
        assertFalse(output.noReviewsYet());
        assertEquals(1L, output.ratingDistribution().get(5));
    }

    @Test
    @DisplayName("Should return special state (NO_REVIEWS_YET) when product has no reviews")
    void shouldReturnSpecialStateWhenNoReviews() {
        // Given
        UUID productId = UUID.randomUUID();
        when(findProductMetricsByProductId.findByProductId(productId)).thenReturn(Optional.empty());

        // When
        GetProductMetricsOutput output = useCase.getMetrics(productId);

        // Then
        assertNotNull(output);
        assertEquals(productId, output.productId());
        assertTrue(output.noReviewsYet());
        assertEquals(0L, output.totalReviews());
        assertNull(output.averageRating());
        
        // Distribution should be all zeros or empty depending on DTO design, 
        // here we assume zeroed buckets for consistency.
        for (int i = 1; i <= 5; i++) {
            assertEquals(0L, output.ratingDistribution().getOrDefault(i, 0L));
        }
    }
}
