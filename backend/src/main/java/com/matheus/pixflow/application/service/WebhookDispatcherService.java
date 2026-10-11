package com.matheus.pixflow.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.WebhookDeliveryResponse;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.SpringDataWebhookRepository;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.WebhookDeliveryJpaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matheus.pixflow.domain.exception.ChargeNotFoundException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class WebhookDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatcherService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SpringDataWebhookRepository webhookRepository;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public WebhookDispatcherService(
            SpringDataWebhookRepository webhookRepository,
            ObjectMapper objectMapper,
            @Value("${pixflow.webhooks.secret-key:pixflow-webhook-secret-2026}") String secretKey
    ) {
        this.webhookRepository = webhookRepository;
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    public void dispatch(Charge charge, String eventType) {
        try {
            Map<String, Object> payloadMap = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "timestamp", Instant.now().toString(),
                    "data", ChargeResponse.fromDomain(charge)
            );

            String jsonPayload = objectMapper.writeValueAsString(payloadMap);
            String signature = calculateHmacSha256(jsonPayload, secretKey);

            WebhookDeliveryJpaEntity delivery = WebhookDeliveryJpaEntity.create(
                    charge.getId(),
                    eventType,
                    jsonPayload,
                    "sha256=" + signature,
                    "DELIVERED",
                    200
            );

            webhookRepository.save(delivery);
            log.info("Webhook event '{}' dispatched and registered for charge {}", eventType, charge.getId());
        } catch (Exception e) {
            log.error("Failed to generate and dispatch webhook for charge {}: {}", charge.getId(), e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getRecentDeliveries(int limit) {
        int safeLimit = Math.max(1, Math.min(50, limit));
        return webhookRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(WebhookDeliveryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getDeliveriesByCharge(UUID chargeId) {
        return webhookRepository.findByChargeIdOrderByCreatedAtDesc(chargeId)
                .stream()
                .map(WebhookDeliveryResponse::fromEntity)
                .toList();
    }

    public WebhookDeliveryResponse replay(UUID deliveryId) {
        WebhookDeliveryJpaEntity original = webhookRepository.findById(deliveryId)
                .orElseThrow(() -> new ChargeNotFoundException(deliveryId));

        String newSignature = calculateHmacSha256(original.getPayload(), secretKey);
        WebhookDeliveryJpaEntity replayed = WebhookDeliveryJpaEntity.create(
                original.getChargeId(),
                original.getEventType() + ".REPLAY",
                original.getPayload(),
                "sha256=" + newSignature,
                "DELIVERED",
                200
        );

        WebhookDeliveryJpaEntity saved = webhookRepository.save(replayed);
        log.info("Webhook replay triggered for delivery {} (charge {})", deliveryId, original.getChargeId());
        return WebhookDeliveryResponse.fromEntity(saved);
    }

    public boolean verifySignature(String payload, String providedSignature) {
        if (payload == null || providedSignature == null) return false;
        String rawSig = providedSignature.startsWith("sha256=")
                ? providedSignature.substring(7)
                : providedSignature;
        String calculated = calculateHmacSha256(payload, secretKey);
        return MessageDigest.isEqual(
                calculated.toLowerCase().getBytes(StandardCharsets.UTF_8),
                rawSig.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String calculateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }
}

