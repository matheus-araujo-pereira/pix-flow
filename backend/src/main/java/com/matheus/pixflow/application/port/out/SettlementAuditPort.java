package com.matheus.pixflow.application.port.out;

import com.matheus.pixflow.domain.model.TransactionStatus;

import java.util.UUID;

public interface SettlementAuditPort {
    void recordTransition(
            UUID chargeId,
            TransactionStatus previousStatus,
            TransactionStatus newStatus,
            String workerId,
            Long latencyMs,
            String details
    );
}
