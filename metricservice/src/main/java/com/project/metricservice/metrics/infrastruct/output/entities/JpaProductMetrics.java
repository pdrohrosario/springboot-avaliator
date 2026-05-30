package com.project.metricservice.metrics.infrastruct.output.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_metrics", schema = "metric_schema")
public class JpaProductMetrics {

    @Id
    private UUID productId;
    
    private long totalReviews;
    private long ratingSum;
    private double averageRating;
    
    private long rating1Count;
    private long rating2Count;
    private long rating3Count;
    private long rating4Count;
    private long rating5Count;
    
    private LocalDateTime updatedAt;

    public JpaProductMetrics() {
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }

    public long getRatingSum() { return ratingSum; }
    public void setRatingSum(long ratingSum) { this.ratingSum = ratingSum; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public long getRating1Count() { return rating1Count; }
    public void setRating1Count(long rating1Count) { this.rating1Count = rating1Count; }

    public long getRating2Count() { return rating2Count; }
    public void setRating2Count(long rating2Count) { this.rating2Count = rating2Count; }

    public long getRating3Count() { return rating3Count; }
    public void setRating3Count(long rating3Count) { this.rating3Count = rating3Count; }

    public long getRating4Count() { return rating4Count; }
    public void setRating4Count(long rating4Count) { this.rating4Count = rating4Count; }

    public long getRating5Count() { return rating5Count; }
    public void setRating5Count(long rating5Count) { this.rating5Count = rating5Count; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
