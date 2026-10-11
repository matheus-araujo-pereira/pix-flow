package com.matheus.pixflow.domain.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    SETTLED,
    FAILED,
    CANCELLED;

    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS = Map.of(
        PENDING, EnumSet.of(PROCESSING, CANCELLED),
        PROCESSING, EnumSet.of(SETTLED, FAILED),
        SETTLED, EnumSet.noneOf(TransactionStatus.class),
        FAILED, EnumSet.noneOf(TransactionStatus.class),
        CANCELLED, EnumSet.noneOf(TransactionStatus.class)
    );

    public boolean canTransitionTo(TransactionStatus target) {
        if (target == null) return false;
        Set<TransactionStatus> nextValidStates = ALLOWED_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(TransactionStatus.class));
        return nextValidStates.contains(target);
    }

    public boolean isTerminal() {
        return this == SETTLED || this == FAILED || this == CANCELLED;
    }
}

