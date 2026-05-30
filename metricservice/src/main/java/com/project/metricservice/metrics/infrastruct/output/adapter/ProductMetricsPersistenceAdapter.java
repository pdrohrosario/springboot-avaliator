package com.project.metricservice.metrics.infrastruct.output.adapter;

import com.project.metricservice.metrics.application.ports.output.FindProductMetricsByProductId;
import com.project.metricservice.metrics.application.ports.output.LoadOrCreateProductMetrics;
import com.project.metricservice.metrics.application.ports.output.SaveProductMetrics;
import com.project.metricservice.metrics.domain.ProductMetrics;
import com.project.metricservice.metrics.infrastruct.output.mapper.ProductMetricsPersistenceMapper;
import com.project.metricservice.metrics.infrastruct.output.repository.JpaProductMetricsRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProductMetricsPersistenceAdapter implements FindProductMetricsByProductId, LoadOrCreateProductMetrics, SaveProductMetrics {

    private final JpaProductMetricsRepository repository;

    public ProductMetricsPersistenceAdapter(JpaProductMetricsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductMetrics> findByProductId(UUID productId) {
        return repository.findById(productId)
                .map(ProductMetricsPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public ProductMetrics loadOrCreate(UUID productId) {
        return repository.findById(productId)
                .map(ProductMetricsPersistenceMapper::toDomain)
                .orElseGet(() -> ProductMetrics.create(productId));
    }

    @Override
    @Transactional
    public void save(ProductMetrics productMetrics) {
        repository.save(ProductMetricsPersistenceMapper.toEntity(productMetrics));
    }
}
