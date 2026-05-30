package com.project.metricservice.metrics.infrastruct.output.repository;

import com.project.metricservice.metrics.infrastruct.output.entities.JpaProcessedReviewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaProcessedReviewEventRepository extends JpaRepository<JpaProcessedReviewEvent, UUID> {
}
