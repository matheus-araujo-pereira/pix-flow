package com.matheus.pixflow.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementMessage(
        UUID chargeId,
        String txId,
        BigDecimal amount,
        Instant dispatchedAt
) {
    public static SettlementMessage of(UUID chargeId, String txId, BigDecimal amount) {
        return new SettlementMessage(chargeId, txId, amount, Instant.now());
    }
}

