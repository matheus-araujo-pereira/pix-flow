package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.domain.model.PixKeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Requisição para disparo concorrente de cobranças em lote (Simulação de Carga)")
public record BatchChargeRequest(
        @Schema(description = "Quantidade de transações a emitir em paralelo (1 a 20)", example = "5")
        @Min(value = 1, message = "Mínimo de 1 transação no lote")
        @Max(value = 20, message = "Máximo de 20 transações no lote para preservação de recursos")
        int count,

        @Schema(description = "Valor unitário de cada transação no lote", example = "75.00")
        @NotNull(message = "Valor da transação é obrigatório")
        BigDecimal amount,

        @Schema(description = "Tipo de chave para o lote", example = "EMAIL")
        PixKeyType pixKeyType,

        @Schema(description = "Prefixo descritivo do lote", example = "Carga de Teste Batch")
        String baseDescription
) {
}

