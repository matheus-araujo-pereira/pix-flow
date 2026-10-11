package com.matheus.pixflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resultado do processamento do lote concorrente de cobranças")
public record BatchChargeResponse(
        @Schema(description = "Total de transações solicitadas", example = "5")
        int totalRequested,

        @Schema(description = "Total de transações criadas com sucesso", example = "5")
        int totalCreated,

        @Schema(description = "Duração total da emissão concorrente em milissegundos", example = "124")
        long totalDurationMs,

        @Schema(description = "Lista das transações geradas")
        List<ChargeResponse> charges
) {
}
