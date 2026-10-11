package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.infrastructure.adapter.out.persistence.WebhookDeliveryJpaEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Registro de notificação e entrega de webhook assinado")
public record WebhookDeliveryResponse(
        UUID id,
        UUID chargeId,
        String eventType,
        String payload,
        String signature,
        String status,
        Integer responseCode,
        Instant createdAt
) {
    public static WebhookDeliveryResponse fromEntity(WebhookDeliveryJpaEntity entity) {
        return new WebhookDeliveryResponse(
                entity.getId(),
                entity.getChargeId(),
                entity.getEventType(),
                entity.getPayload(),
                entity.getSignature(),
                entity.getStatus(),
                entity.getResponseCode(),
                entity.getCreatedAt()
        );
    }
}

