package com.project.metricservice.metrics.infrastruct.input.api;

import com.project.metricservice.metrics.application.output.GetProductMetricsOutput;
import com.project.metricservice.metrics.application.ports.input.GetProductMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/metrics/product")
public class ProductMetricsController {

    private final GetProductMetrics getProductMetrics;

    public ProductMetricsController(GetProductMetrics getProductMetrics) {
        this.getProductMetrics = getProductMetrics;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GetProductMetricsOutput> getMetrics(@PathVariable UUID productId) {
        GetProductMetricsOutput output = getProductMetrics.getMetrics(productId);
        return ResponseEntity.ok(output);
    }
}
