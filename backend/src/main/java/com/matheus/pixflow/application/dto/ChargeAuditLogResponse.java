package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.SettlementAuditLogJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Evento da linha do tempo e auditoria da transação")
public record ChargeAuditLogResponse(
        UUID id,
        UUID chargeId,
        TransactionStatus previousStatus,
        TransactionStatus newStatus,
        String workerId,
        Long latencyMs,
        String details,
        Instant createdAt
) {
    public static ChargeAuditLogResponse fromEntity(SettlementAuditLogJpaEntity entity) {
        return new ChargeAuditLogResponse(
                entity.getId(),
                entity.getChargeId(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getWorkerId(),
                entity.getLatencyMs(),
                entity.getDetails(),
                entity.getCreatedAt()
        );
    }
}

