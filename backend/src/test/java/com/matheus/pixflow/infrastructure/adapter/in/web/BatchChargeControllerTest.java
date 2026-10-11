package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.BatchChargeRequest;
import com.matheus.pixflow.application.dto.BatchChargeResponse;
import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.service.BatchChargeService;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BatchChargeController.class)
class BatchChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BatchChargeService batchChargeService;

    @Test
    @DisplayName("Deve disparar lote concorrente com sucesso via POST /api/v1/charges/batch")
    void shouldExecuteBatchSuccessfully() throws Exception {
        ChargeResponse item = new ChargeResponse(
                UUID.randomUUID(),
                "tx12345",
                "E00000000202610110445a8b9c1d2e3f",
                "52998224725",
                PixKeyType.CPF,
                new BigDecimal("50.00"),
                "Batch Test #01",
                TransactionStatus.PENDING,
                "000201...",
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        BatchChargeResponse response = new BatchChargeResponse(1, 1, 120L, List.of(item));
        when(batchChargeService.executeBatch(any(BatchChargeRequest.class))).thenReturn(response);

        BatchChargeRequest request = new BatchChargeRequest(
                5,
                new BigDecimal("25.00"),
                PixKeyType.CPF,
                "Stress Test"
        );

        mockMvc.perform(post("/api/v1/charges/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested").value(1))
                .andExpect(jsonPath("$.totalCreated").value(1))
                .andExpect(jsonPath("$.totalDurationMs").value(120L))
                .andExpect(jsonPath("$.charges[0].txId").value("tx12345"));
    }
}
