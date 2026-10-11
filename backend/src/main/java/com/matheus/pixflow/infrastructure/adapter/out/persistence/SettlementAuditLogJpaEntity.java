package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.domain.model.TransactionStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_audit_logs")
public class SettlementAuditLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "charge_id", nullable = false)
    private UUID chargeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private TransactionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private TransactionStatus newStatus;

    @Column(name = "worker_id", nullable = false, length = 100)
    private String workerId;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SettlementAuditLogJpaEntity() {}

    public static SettlementAuditLogJpaEntity create(
            UUID chargeId,
            TransactionStatus previousStatus,
            TransactionStatus newStatus,
            String workerId,
            Long latencyMs,
            String details
    ) {
        SettlementAuditLogJpaEntity entity = new SettlementAuditLogJpaEntity();
        entity.id = UUID.randomUUID();
        entity.chargeId = chargeId;
        entity.previousStatus = previousStatus;
        entity.newStatus = newStatus;
        entity.workerId = workerId;
        entity.latencyMs = latencyMs;
        entity.details = details;
        entity.createdAt = Instant.now();
        return entity;
    }

    public UUID getId() { return id; }
    public UUID getChargeId() { return chargeId; }
    public TransactionStatus getPreviousStatus() { return previousStatus; }
    public TransactionStatus getNewStatus() { return newStatus; }
    public String getWorkerId() { return workerId; }
    public Long getLatencyMs() { return latencyMs; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}

