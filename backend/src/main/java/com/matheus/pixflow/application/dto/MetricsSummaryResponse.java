package com.matheus.pixflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Aggregated settlement engine metrics")
public record MetricsSummaryResponse(
        @Schema(description = "Total transactions created", example = "142")
        long totalTransactions,

        @Schema(description = "Total settled transactions", example = "136")
        long settledCount,

        @Schema(description = "Currently pending transactions", example = "3")
        long pendingCount,

        @Schema(description = "Currently processing transactions in queue", example = "1")
        long processingCount,

        @Schema(description = "Failed transactions", example = "2")
        long failedCount,

        @Schema(description = "Cancelled transactions", example = "0")
        long cancelledCount,

        @Schema(description = "Total financial volume settled in BRL", example = "45890.50")
        BigDecimal totalSettledVolume,

        @Schema(description = "Average settlement duration in milliseconds", example = "1240.5")
        Double averageSettlementTimeMs,

        @Schema(description = "Settlement success rate percentage", example = "98.5")
        Double successRatePercentage,

        @Schema(description = "Overall engine operational status", example = "OPERATIONAL")
        String systemStatus
) {
}
