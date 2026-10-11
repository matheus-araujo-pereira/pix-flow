package com.matheus.pixflow.application.dto;

import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.SettlementAuditLogJpaEntity;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.WebhookDeliveryJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    @DisplayName("Deve testar PageResponse com tamanho zero e maior que zero")
    void shouldTestPageResponse() {
        PageResponse<String> pageZero = PageResponse.of(List.of(), 0, 0, 0);
        assertEquals(1, pageZero.totalPages());
        assertEquals(0, pageZero.totalElements());
        assertEquals(0, pageZero.content().size());

        PageResponse<String> pageStandard = PageResponse.of(List.of("a", "b"), 1, 10, 25);
        assertEquals(3, pageStandard.totalPages());
        assertEquals(25, pageStandard.totalElements());
        assertEquals(1, pageStandard.page());
        assertEquals(10, pageStandard.size());
    }

    @Test
    @DisplayName("Deve testar ChargeResponse fromDomain com null")
    void shouldTestChargeResponseFromDomainNull() {
        assertNull(ChargeResponse.fromDomain(null));
    }

    @Test
    @DisplayName("Deve testar WebhookDeliveryResponse fromEntity e getters")
    void shouldTestWebhookDeliveryResponse() {
        UUID id = UUID.randomUUID();
        UUID chargeId = UUID.randomUUID();
        Instant now = Instant.now();

        WebhookDeliveryJpaEntity entity = WebhookDeliveryJpaEntity.create(
                chargeId,
                "PIX_PAYMENT_SETTLED",
                "{}",
                "sha256=123",
                "DELIVERED",
                200
        );

        WebhookDeliveryResponse response = WebhookDeliveryResponse.fromEntity(entity);
        assertEquals(chargeId, response.chargeId());
        assertEquals("PIX_PAYMENT_SETTLED", response.eventType());
        assertEquals("sha256=123", response.signature());
        assertEquals(200, response.responseCode());
        assertEquals("DELIVERED", response.status());
    }

    @Test
    @DisplayName("Deve testar ChargeAuditLogResponse fromEntity e getters")
    void shouldTestChargeAuditLogResponse() {
        UUID chargeId = UUID.randomUUID();
        SettlementAuditLogJpaEntity entity = SettlementAuditLogJpaEntity.create(
                chargeId,
                TransactionStatus.PENDING,
                TransactionStatus.PROCESSING,
                "WORKER-01",
                120L,
                "Audit details"
        );

        ChargeAuditLogResponse response = ChargeAuditLogResponse.fromEntity(entity);
        assertEquals(chargeId, response.chargeId());
        assertEquals(TransactionStatus.PENDING, response.previousStatus());
        assertEquals(TransactionStatus.PROCESSING, response.newStatus());
        assertEquals("WORKER-01", response.workerId());
        assertEquals(120L, response.latencyMs());
        assertEquals("Audit details", response.details());
    }

    @Test
    @DisplayName("Deve testar BatchChargeRequest e BatchChargeResponse")
    void shouldTestBatchDtos() {
        BatchChargeRequest req = new BatchChargeRequest(
                5,
                new BigDecimal("20.00"),
                PixKeyType.CNPJ,
                "Batch Test"
        );
        assertEquals(5, req.count());
        assertEquals(new BigDecimal("20.00"), req.amount());
        assertEquals(PixKeyType.CNPJ, req.pixKeyType());
        assertEquals("Batch Test", req.baseDescription());

        BatchChargeResponse resp = new BatchChargeResponse(5, 5, 100L, List.of());
        assertEquals(5, resp.totalRequested());
        assertEquals(5, resp.totalCreated());
        assertEquals(100L, resp.totalDurationMs());
        assertTrue(resp.charges().isEmpty());
    }

    @Test
    @DisplayName("Deve testar SettlementMessage e ErrorResponse")
    void shouldTestOtherDtos() {
        UUID chargeId = UUID.randomUUID();
        Instant now = Instant.now();
        SettlementMessage msg = new SettlementMessage(chargeId, "tx1", new BigDecimal("10.00"), now);
        assertEquals(chargeId, msg.chargeId());
        assertEquals("tx1", msg.txId());
        assertEquals(new BigDecimal("10.00"), msg.amount());
        assertEquals(now, msg.dispatchedAt());

        ErrorResponse err = ErrorResponse.of(404, "Not Found", "Item not found", "/api/test");
        assertEquals(404, err.status());
        assertEquals("Not Found", err.error());
        assertEquals("Item not found", err.message());
        assertEquals("/api/test", err.path());
        assertNull(err.fieldErrors());

        ErrorResponse valErr = ErrorResponse.ofValidation(400, "Bad Request", "Validation failed", "/api/test", Map.of("field", "error"));
        assertEquals(400, valErr.status());
        assertEquals(1, valErr.fieldErrors().size());
    }
}
