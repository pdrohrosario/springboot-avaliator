package com.project.metricservice.metrics.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductMetrics {

    private final UUID productId;
    private long totalReviews;
    private long ratingSum;
    private double averageRating;
    private final Map<Integer, Long> ratingDistribution;

    private ProductMetrics(UUID productId, long totalReviews, long ratingSum, double averageRating, Map<Integer, Long> ratingDistribution) {
        this.productId = productId;
        this.totalReviews = totalReviews;
        this.ratingSum = ratingSum;
        this.averageRating = averageRating;
        this.ratingDistribution = ratingDistribution;
    }

    public static ProductMetrics create(UUID productId) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        return new ProductMetrics(productId, 0L, 0L, 0.0, distribution);
    }

    public static ProductMetrics fromEntity(UUID productId, long totalReviews, long ratingSum, double averageRating, Map<Integer, Long> ratingDistribution) {
        return new ProductMetrics(productId, totalReviews, ratingSum, averageRating, new HashMap<>(ratingDistribution));
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        totalReviews++;
        ratingSum += rating;
        averageRating = (double) ratingSum / totalReviews;
        
        long currentCount = ratingDistribution.getOrDefault(rating, 0L);
        ratingDistribution.put(rating, currentCount + 1);
    }

    public UUID getProductId() {
        return productId;
    }

    public long getTotalReviews() {
        return totalReviews;
    }

    public long getRatingSum() {
        return ratingSum;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return new HashMap<>(ratingDistribution);
    }
}
