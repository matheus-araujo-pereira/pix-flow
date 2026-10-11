package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.CreateChargeRequest;
import com.matheus.pixflow.application.dto.PageResponse;
import com.matheus.pixflow.application.port.in.CancelChargeUseCase;
import com.matheus.pixflow.application.port.in.CreateChargeUseCase;
import com.matheus.pixflow.application.port.in.GetChargeUseCase;
import com.matheus.pixflow.application.port.in.SettleChargeUseCase;
import com.matheus.pixflow.domain.exception.ChargeNotFoundException;
import com.matheus.pixflow.domain.exception.InvalidStatusTransitionException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargeController.class)
class ChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateChargeUseCase createChargeUseCase;

    @MockBean
    private GetChargeUseCase getChargeUseCase;

    @MockBean
    private CancelChargeUseCase cancelChargeUseCase;

    @MockBean
    private SettleChargeUseCase settleChargeUseCase;

    private ChargeResponse buildMockResponse(UUID id, TransactionStatus status) {
        return new ChargeResponse(
                id,
                "tx" + id.toString().replace("-", "").substring(0, 24),
                "E00000000202610111200abc123456789",
                "12345678909",
                PixKeyType.CPF,
                new BigDecimal("150.00"),
                "Test Charge",
                status,
                "00020126...mock",
                null,
                Instant.now(),
                Instant.now(),
                status == TransactionStatus.SETTLED ? Instant.now() : null
        );
    }

    @Test
    @DisplayName("POST /api/v1/charges should create charge and return 201")
    void shouldCreateChargeSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.PENDING);

        when(createChargeUseCase.create(any(CreateChargeRequest.class))).thenReturn(response);

        CreateChargeRequest request = new CreateChargeRequest(
                "12345678909",
                PixKeyType.CPF,
                new BigDecimal("150.00"),
                "Test Charge"
        );

        mockMvc.perform(post("/api/v1/charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    @DisplayName("POST /api/v1/charges should return 400 Bad Request on validation error")
    void shouldReturn400OnValidationFailure() throws Exception {
        CreateChargeRequest invalidRequest = new CreateChargeRequest(
                "", // Blank
                null, // Null key type
                new BigDecimal("-10.00"), // Negative amount
                "Invalid"
        );

        mockMvc.perform(post("/api/v1/charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    @DisplayName("GET /api/v1/charges should return paginated list")
    void shouldListCharges() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.SETTLED);
        PageResponse<ChargeResponse> pageResponse = PageResponse.of(List.of(response), 0, 20, 1);

        when(getChargeUseCase.listAll(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/charges?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/charges/{id} should return charge when found")
    void shouldGetChargeById() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.PENDING);

        when(getChargeUseCase.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/charges/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/charges/{id} should return 404 when not found")
    void shouldReturn404WhenIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(getChargeUseCase.getById(id)).thenThrow(new ChargeNotFoundException(id));

        mockMvc.perform(get("/api/v1/charges/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/charges/tx/{txId} should return charge")
    void shouldGetChargeByTxId() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.PENDING);

        when(getChargeUseCase.getByTxId("tx123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/charges/tx/{txId}", "tx123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/charges/{id}/cancel should return 200 OK")
    void shouldCancelCharge() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.CANCELLED);

        when(cancelChargeUseCase.cancel(id)).thenReturn(response);

        mockMvc.perform(post("/api/v1/charges/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("POST /api/v1/charges/{id}/cancel should return 409 Conflict if status cannot transition")
    void shouldReturn409OnInvalidCancel() throws Exception {
        UUID id = UUID.randomUUID();
        when(cancelChargeUseCase.cancel(id)).thenThrow(
                new InvalidStatusTransitionException(TransactionStatus.SETTLED, TransactionStatus.CANCELLED));

        mockMvc.perform(post("/api/v1/charges/{id}/cancel", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/v1/charges/{id}/settle should trigger settlement and return 200 OK")
    void shouldForceSettle() throws Exception {
        UUID id = UUID.randomUUID();
        ChargeResponse response = buildMockResponse(id, TransactionStatus.SETTLED);

        when(settleChargeUseCase.triggerSettlement(id)).thenReturn(response);

        mockMvc.perform(post("/api/v1/charges/{id}/settle", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SETTLED"));
    }
}
