package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charges")
public class ChargeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tx_id", nullable = false, unique = true, length = 35)
    private String txId;

    @Column(name = "end_to_end_id", nullable = false, unique = true, length = 32)
    private String endToEndId;

    @Column(name = "pix_key", nullable = false, length = 140)
    private String pixKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "pix_key_type", nullable = false, length = 20)
    private PixKeyType pixKeyType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "qr_code_payload", nullable = false, columnDefinition = "TEXT")
    private String qrCodePayload;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    public ChargeJpaEntity() {}

    public static ChargeJpaEntity fromDomain(Charge domain) {
        ChargeJpaEntity entity = new ChargeJpaEntity();
        entity.id = domain.getId();
        entity.txId = domain.getTxId();
        entity.endToEndId = domain.getEndToEndId();
        entity.pixKey = domain.getPixKey();
        entity.pixKeyType = domain.getPixKeyType();
        entity.amount = domain.getAmount();
        entity.description = domain.getDescription();
        entity.status = domain.getStatus();
        entity.qrCodePayload = domain.getQrCodePayload();
        entity.failureReason = domain.getFailureReason();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.settledAt = domain.getSettledAt();
        return entity;
    }

    public Charge toDomain() {
        return Charge.restore(
                this.id,
                this.txId,
                this.endToEndId,
                this.pixKey,
                this.pixKeyType,
                this.amount,
                this.description,
                this.status,
                this.qrCodePayload,
                this.failureReason,
                this.createdAt,
                this.updatedAt,
                this.settledAt
        );
    }

    public UUID getId() { return id; }
    public String getTxId() { return txId; }
    public String getEndToEndId() { return endToEndId; }
    public String getPixKey() { return pixKey; }
    public PixKeyType getPixKeyType() { return pixKeyType; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public TransactionStatus getStatus() { return status; }
    public String getQrCodePayload() { return qrCodePayload; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSettledAt() { return settledAt; }
}

