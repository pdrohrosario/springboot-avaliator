package com.project.metricservice.metrics.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductMetrics aggregate.
 * Covers US-05: Update product metrics from review-created event.
 */
class ProductMetricsTest {

    @Test
    @DisplayName("Should create initial metrics for a product")
    void shouldCreateInitialMetrics() {
        UUID productId = UUID.randomUUID();
        ProductMetrics metrics = ProductMetrics.create(productId);

        assertNotNull(metrics);
        assertEquals(productId, metrics.getProductId());
        assertEquals(0L, metrics.getTotalReviews());
        assertEquals(0L, metrics.getRatingSum());
        assertEquals(0.0, metrics.getAverageRating());
        
        // Distribution should have all buckets (1..5) at zero
        assertNotNull(metrics.getRatingDistribution());
        for (int i = 1; i <= 5; i++) {
            assertEquals(0L, metrics.getRatingDistribution().getOrDefault(i, 0L), "Bucket " + i + " should be 0");
        }
    }

    @Test
    @DisplayName("Should add a rating and update metrics correctly")
    void shouldAddRatingAndUpdateMetrics() {
        UUID productId = UUID.randomUUID();
        ProductMetrics metrics = ProductMetrics.create(productId);

        // Add first rating: 5
        metrics.addRating(5);

        assertEquals(1L, metrics.getTotalReviews());
        assertEquals(5L, metrics.getRatingSum());
        assertEquals(5.0, metrics.getAverageRating());
        assertEquals(1L, metrics.getRatingDistribution().get(5));

        // Add second rating: 4
        metrics.addRating(4);

        assertEquals(2L, metrics.getTotalReviews());
        assertEquals(9L, metrics.getRatingSum());
        assertEquals(4.5, metrics.getAverageRating());
        assertEquals(1L, metrics.getRatingDistribution().get(4));
        assertEquals(1L, metrics.getRatingDistribution().get(5));
        
        // Add third rating: 4
        metrics.addRating(4);
        
        assertEquals(3L, metrics.getTotalReviews());
        assertEquals(13L, metrics.getRatingSum());
        assertEquals(4.33, metrics.getAverageRating(), 0.01);
        assertEquals(2L, metrics.getRatingDistribution().get(4));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    @DisplayName("Should throw exception when rating is out of range 1-5")
    void shouldThrowExceptionWhenRatingIsOutOfRange(int invalidRating) {
        ProductMetrics metrics = ProductMetrics.create(UUID.randomUUID());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> metrics.addRating(invalidRating));
        assertTrue(exception.getMessage().contains("Rating must be between 1 and 5"));
    }

    @Test
    @DisplayName("Should reconstitute metrics from persistence")
    void shouldReconstituteMetrics() {
        UUID productId = UUID.randomUUID();
        long totalReviews = 10;
        long ratingSum = 45;
        double averageRating = 4.5;
        java.util.Map<Integer, Long> distribution = new java.util.HashMap<>();
        distribution.put(5, 5L);
        distribution.put(4, 5L);

        ProductMetrics metrics = ProductMetrics.fromEntity(productId, totalReviews, ratingSum, averageRating, distribution);

        assertEquals(productId, metrics.getProductId());
        assertEquals(totalReviews, metrics.getTotalReviews());
        assertEquals(ratingSum, metrics.getRatingSum());
        assertEquals(averageRating, metrics.getAverageRating());
        assertEquals(5L, metrics.getRatingDistribution().get(5));
    }
}
