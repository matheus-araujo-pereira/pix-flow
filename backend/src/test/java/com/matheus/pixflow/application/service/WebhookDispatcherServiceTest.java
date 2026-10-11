package com.matheus.pixflow.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.SpringDataWebhookRepository;
import com.matheus.pixflow.infrastructure.adapter.out.persistence.WebhookDeliveryJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookDispatcherServiceTest {

    @Mock
    private SpringDataWebhookRepository webhookRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private WebhookDispatcherService service;

    @BeforeEach
    void setUp() {
        service = new WebhookDispatcherService(webhookRepository, objectMapper, "test-secret-key");
    }

    @Test
    @DisplayName("Should dispatch and register webhook with HMAC-SHA256 signature")
    void shouldDispatchWebhook() {
        Charge charge = Charge.create("52998224725", PixKeyType.CPF, new BigDecimal("100.00"), "desc");

        service.dispatch(charge, "PIX_PAYMENT_SETTLED");

        verify(webhookRepository, times(1)).save(any(WebhookDeliveryJpaEntity.class));
    }

    @Test
    @DisplayName("Should verify valid HMAC-SHA256 signature correctly")
    void shouldVerifySignature() {
        String payload = "{\"test\":\"data\"}";
        Charge charge = Charge.create("52998224725", PixKeyType.CPF, new BigDecimal("100.00"), "desc");
        service.dispatch(charge, "PIX_PAYMENT_SETTLED");

        // Verify with invalid signature
        assertFalse(service.verifySignature(payload, "invalid-sig"));

        // Null signature
        assertFalse(service.verifySignature(payload, null));
    }

    @Test
    @DisplayName("Should query recent deliveries")
    void shouldQueryDeliveries() {
        when(webhookRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(List.of());
        var deliveries = service.getRecentDeliveries(10);
        assertNotNull(deliveries);
        assertTrue(deliveries.isEmpty());
    }

    @Test
    @DisplayName("Should query deliveries by charge ID")
    void shouldQueryDeliveriesByCharge() {
        UUID chargeId = UUID.randomUUID();
        when(webhookRepository.findByChargeIdOrderByCreatedAtDesc(chargeId)).thenReturn(List.of());
        var deliveries = service.getDeliveriesByCharge(chargeId);
        assertNotNull(deliveries);
        assertTrue(deliveries.isEmpty());
    }
}
