package com.project.metricservice.metrics.infrastruct.output.adapter;

import com.project.metricservice.metrics.application.ports.output.ExistsProcessedReviewEvent;
import com.project.metricservice.metrics.application.ports.output.RegisterProcessedReviewEvent;
import com.project.metricservice.metrics.infrastruct.output.entities.JpaProcessedReviewEvent;
import com.project.metricservice.metrics.infrastruct.output.repository.JpaProcessedReviewEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ProcessedReviewEventAdapter implements ExistsProcessedReviewEvent, RegisterProcessedReviewEvent {

    private final JpaProcessedReviewEventRepository repository;

    public ProcessedReviewEventAdapter(JpaProcessedReviewEventRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID reviewId) {
        return repository.existsById(reviewId);
    }

    @Override
    @Transactional
    public void register(UUID reviewId) {
        JpaProcessedReviewEvent entity = new JpaProcessedReviewEvent();
        entity.setReviewId(reviewId);
        entity.setProcessedAt(LocalDateTime.now());
        repository.save(entity);
    }
}
