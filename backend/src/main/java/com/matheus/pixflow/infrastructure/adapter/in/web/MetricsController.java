package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.application.dto.MetricsSummaryResponse;
import com.matheus.pixflow.application.port.in.GetMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
@Tag(name = "Telemetry & Metrics", description = "Real-time metrics, throughput, latency, and settlement engine status")
public class MetricsController {

    private final GetMetricsUseCase getMetricsUseCase;

    public MetricsController(GetMetricsUseCase getMetricsUseCase) {
        this.getMetricsUseCase = getMetricsUseCase;
    }

    @GetMapping
    @Operation(summary = "Get engine metrics", description = "Returns aggregated transaction volume, status distributions, average latency, and health state.")
    @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    public ResponseEntity<MetricsSummaryResponse> getMetrics() {
        MetricsSummaryResponse metrics = getMetricsUseCase.getMetrics();
        return ResponseEntity.ok(metrics);
    }
}
