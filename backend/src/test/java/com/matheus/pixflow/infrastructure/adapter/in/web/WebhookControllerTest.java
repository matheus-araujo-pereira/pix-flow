package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.WebhookDeliveryResponse;
import com.matheus.pixflow.application.service.WebhookDispatcherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebhookDispatcherService webhookDispatcherService;

    @Test
    @DisplayName("Deve listar notificações recentes de webhook")
    void shouldGetRecentDeliveries() throws Exception {
        WebhookDeliveryResponse response = new WebhookDeliveryResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PIX_PAYMENT_SETTLED",
                "{}",
                "sha256=abcdef",
                "DELIVERED",
                200,
                Instant.now()
        );

        when(webhookDispatcherService.getRecentDeliveries(20)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/webhooks/deliveries?limit=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("PIX_PAYMENT_SETTLED"))
                .andExpect(jsonPath("$[0].responseCode").value(200));
    }

    @Test
    @DisplayName("Deve listar webhooks filtrados por chargeId")
    void shouldGetDeliveriesByCharge() throws Exception {
        UUID chargeId = UUID.randomUUID();
        WebhookDeliveryResponse response = new WebhookDeliveryResponse(
                UUID.randomUUID(),
                chargeId,
                "PIX_PAYMENT_FAILED",
                "{}",
                "sha256=123456",
                "DELIVERED",
                200,
                Instant.now()
        );

        when(webhookDispatcherService.getDeliveriesByCharge(chargeId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/webhooks/charges/" + chargeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargeId").value(chargeId.toString()))
                .andExpect(jsonPath("$[0].eventType").value("PIX_PAYMENT_FAILED"));
    }

    @Test
    @DisplayName("Deve verificar assinatura HMAC com sucesso")
    void shouldVerifySignature() throws Exception {
        when(webhookDispatcherService.verifySignature("payload_data", "valid_signature")).thenReturn(true);

        Map<String, String> request = Map.of(
                "payload", "payload_data",
                "signature", "valid_signature"
        );

        mockMvc.perform(post("/api/v1/webhooks/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.algorithm").value("HmacSHA256"));
    }
}
