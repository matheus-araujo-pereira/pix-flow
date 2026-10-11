package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDeliveryJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "charge_id", nullable = false)
    private UUID chargeId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "signature", nullable = false, length = 128)
    private String signature;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WebhookDeliveryJpaEntity() {}

    public static WebhookDeliveryJpaEntity create(
            UUID chargeId,
            String eventType,
            String payload,
            String signature,
            String status,
            Integer responseCode
    ) {
        WebhookDeliveryJpaEntity entity = new WebhookDeliveryJpaEntity();
        entity.id = UUID.randomUUID();
        entity.chargeId = chargeId;
        entity.eventType = eventType;
        entity.payload = payload;
        entity.signature = signature;
        entity.status = status;
        entity.responseCode = responseCode;
        entity.createdAt = Instant.now();
        return entity;
    }

    public UUID getId() { return id; }
    public UUID getChargeId() { return chargeId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getSignature() { return signature; }
    public String getStatus() { return status; }
    public Integer getResponseCode() { return responseCode; }
    public Instant getCreatedAt() { return createdAt; }
}
