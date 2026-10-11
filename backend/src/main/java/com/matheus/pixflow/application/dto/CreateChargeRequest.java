package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.domain.model.PixKeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload for generating a new PIX instant charge")
public record CreateChargeRequest(
        @Schema(description = "PIX Key (CPF, CNPJ, Email, Phone, or EVP Random Key)", example = "12345678909")
        @NotBlank(message = "PIX Key is required")
        String pixKey,

        @Schema(description = "Type of PIX Key", example = "CPF")
        @NotNull(message = "PIX Key Type is required (CPF, CNPJ, EMAIL, PHONE, RANDOM)")
        PixKeyType pixKeyType,

        @Schema(description = "Monetary amount in BRL", example = "150.00")
        @NotNull(message = "Transaction amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least R$ 0.01")
        @DecimalMax(value = "99999999.99", message = "Amount exceeds permissible limit")
        BigDecimal amount,

        @Schema(description = "Optional transaction description or narrative", example = "Payment for Cloud Services")
        String description
) {
}

