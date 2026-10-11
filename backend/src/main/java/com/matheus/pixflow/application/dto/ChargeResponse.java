package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Response details of a PIX transaction")
public record ChargeResponse(
        @Schema(description = "Unique internal identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "PIX Transaction Identifier", example = "tx550e8400e29b41d4a7164466")
        String txId,

        @Schema(description = "BACEN EndToEnd Identifier", example = "E00000000202610110445a8b9c1d2e3f")
        String endToEndId,

        @Schema(description = "Sanitized PIX Key", example = "12345678909")
        String pixKey,

        @Schema(description = "PIX Key Type", example = "CPF")
        PixKeyType pixKeyType,

        @Schema(description = "Amount in BRL", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Transaction narrative or description", example = "Payment for Cloud Services")
        String description,

        @Schema(description = "Current lifecycle status", example = "SETTLED")
        TransactionStatus status,

        @Schema(description = "EMVCo BR Code payload for QR Code rendering and Copia e Cola", example = "00020126360014br.gov.bcb.pix0111123456789095204000053039865406150.005802BR5914PixFlow Engine6008BRASILIA62300526tx550e8400e29b41d4a71644666304E8A2")
        String qrCodePayload,

        @Schema(description = "Reason for failure if status is FAILED", example = "Exceeded daily clearing limit")
        String failureReason,

        @Schema(description = "Charge creation timestamp in UTC", example = "2026-10-11T04:45:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp in UTC", example = "2026-10-11T04:45:01Z")
        Instant updatedAt,

        @Schema(description = "Final settlement timestamp in UTC", example = "2026-10-11T04:45:02Z")
        Instant settledAt
) {
    public static ChargeResponse fromDomain(Charge charge) {
        if (charge == null) return null;
        return new ChargeResponse(
                charge.getId(),
                charge.getTxId(),
                charge.getEndToEndId(),
                charge.getPixKey(),
                charge.getPixKeyType(),
                charge.getAmount(),
                charge.getDescription(),
                charge.getStatus(),
                charge.getQrCodePayload(),
                charge.getFailureReason(),
                charge.getCreatedAt(),
                charge.getUpdatedAt(),
                charge.getSettledAt()
        );
    }
}

