package com.project.metricservice.metrics.infrastruct.output.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_review_events", schema = "metric_schema")
public class JpaProcessedReviewEvent {

    @Id
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public JpaProcessedReviewEvent() {
    }

    public UUID getReviewId() { return reviewId; }
    public void setReviewId(UUID reviewId) { this.reviewId = reviewId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
