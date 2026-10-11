package com.matheus.pixflow.application.port.in;

import com.matheus.pixflow.application.dto.MetricsSummaryResponse;

public interface GetMetricsUseCase {
    MetricsSummaryResponse getMetrics();
}

