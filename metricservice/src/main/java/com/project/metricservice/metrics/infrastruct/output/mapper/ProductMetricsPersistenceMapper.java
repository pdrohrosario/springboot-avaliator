package com.project.metricservice.metrics.infrastruct.output.mapper;

import com.project.metricservice.metrics.domain.ProductMetrics;
import com.project.metricservice.metrics.infrastruct.output.entities.JpaProductMetrics;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ProductMetricsPersistenceMapper {

    public static ProductMetrics toDomain(JpaProductMetrics entity) {
        if (entity == null) return null;

        Map<Integer, Long> distribution = new HashMap<>();
        distribution.put(1, entity.getRating1Count());
        distribution.put(2, entity.getRating2Count());
        distribution.put(3, entity.getRating3Count());
        distribution.put(4, entity.getRating4Count());
        distribution.put(5, entity.getRating5Count());

        return ProductMetrics.fromEntity(
                entity.getProductId(),
                entity.getTotalReviews(),
                entity.getRatingSum(),
                entity.getAverageRating(),
                distribution
        );
    }

    public static JpaProductMetrics toEntity(ProductMetrics domain) {
        if (domain == null) return null;

        JpaProductMetrics entity = new JpaProductMetrics();
        entity.setProductId(domain.getProductId());
        entity.setTotalReviews(domain.getTotalReviews());
        entity.setRatingSum(domain.getRatingSum());
        entity.setAverageRating(domain.getAverageRating());
        
        Map<Integer, Long> distribution = domain.getRatingDistribution();
        entity.setRating1Count(distribution.getOrDefault(1, 0L));
        entity.setRating2Count(distribution.getOrDefault(2, 0L));
        entity.setRating3Count(distribution.getOrDefault(3, 0L));
        entity.setRating4Count(distribution.getOrDefault(4, 0L));
        entity.setRating5Count(distribution.getOrDefault(5, 0L));
        
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
