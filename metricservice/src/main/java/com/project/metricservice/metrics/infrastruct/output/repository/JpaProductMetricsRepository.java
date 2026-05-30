package com.project.metricservice.metrics.infrastruct.output.repository;

import com.project.metricservice.metrics.infrastruct.output.entities.JpaProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaProductMetricsRepository extends JpaRepository<JpaProductMetrics, UUID> {
}
