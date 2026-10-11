package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.BatchChargeRequest;
import com.matheus.pixflow.application.dto.BatchChargeResponse;
import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.port.in.CreateChargeUseCase;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchChargeServiceTest {

    @Mock
    private CreateChargeUseCase createChargeUseCase;

    private final Executor directExecutor = Runnable::run;

    @Test
    @DisplayName("Should execute batch and return aggregated responses")
    void shouldExecuteBatch() {
        BatchChargeService service = new BatchChargeService(createChargeUseCase, directExecutor);

        ChargeResponse mockResponse = new ChargeResponse(
                UUID.randomUUID(),
                "tx-batch-1",
                "E00000000202610111200abc123456789",
                "stress-worker-01@pixflow.com.br",
                PixKeyType.EMAIL,
                new BigDecimal("50.00"),
                "Batch Load Test [#01]",
                TransactionStatus.PENDING,
                "000201...",
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        when(createChargeUseCase.create(any())).thenReturn(mockResponse);

        BatchChargeRequest request = new BatchChargeRequest(
                3,
                new BigDecimal("50.00"),
                PixKeyType.EMAIL,
                "Stress Load"
        );

        BatchChargeResponse response = service.executeBatch(request);

        assertNotNull(response);
        assertEquals(3, response.totalRequested());
        assertEquals(3, response.totalCreated());
        assertEquals(3, response.charges().size());
        verify(createChargeUseCase, times(3)).create(any());
    }

    @Test
    @DisplayName("Should handle default values and multiple key types")
    void shouldHandleDefaultValuesAndKeyTypes() {
        BatchChargeService service = new BatchChargeService(createChargeUseCase, directExecutor);

        ChargeResponse mockResponse = new ChargeResponse(
                UUID.randomUUID(),
                "tx-batch-2",
                "E00000000202610111200abc123456789",
                "52998224725",
                PixKeyType.CPF,
                new BigDecimal("50.00"),
                "Batch Load Test [#01]",
                TransactionStatus.PENDING,
                "000201...",
                null,
                Instant.now(),
                Instant.now(),
                null
        );

        when(createChargeUseCase.create(any())).thenReturn(mockResponse);

        // Test with null amount, null type, null baseDescription
        BatchChargeRequest reqDefaults = new BatchChargeRequest(0, null, null, null);
        BatchChargeResponse resp1 = service.executeBatch(reqDefaults);
        assertEquals(1, resp1.totalRequested());

        // Test with amount <= 0
        service.executeBatch(new BatchChargeRequest(1, BigDecimal.ZERO, PixKeyType.EMAIL, "Zero Amount"));

        // Test with CPF
        service.executeBatch(new BatchChargeRequest(1, BigDecimal.TEN, PixKeyType.CPF, ""));
        // Test with CNPJ
        service.executeBatch(new BatchChargeRequest(1, BigDecimal.TEN, PixKeyType.CNPJ, "Test"));
        // Test with PHONE
        service.executeBatch(new BatchChargeRequest(1, BigDecimal.TEN, PixKeyType.PHONE, "Test"));
        // Test with RANDOM
        service.executeBatch(new BatchChargeRequest(1, BigDecimal.TEN, PixKeyType.RANDOM, "Test"));
    }

    @Test
    @DisplayName("Should handle exceptions thrown by createChargeUseCase")
    void shouldHandleExceptionsDuringBatch() {
        BatchChargeService service = new BatchChargeService(createChargeUseCase, directExecutor);

        when(createChargeUseCase.create(any())).thenThrow(new RuntimeException("DB Connection Timeout"));

        BatchChargeRequest request = new BatchChargeRequest(
                2,
                new BigDecimal("10.00"),
                PixKeyType.EMAIL,
                "Failure Test"
        );

        BatchChargeResponse response = service.executeBatch(request);

        assertNotNull(response);
        assertEquals(2, response.totalRequested());
        assertEquals(0, response.totalCreated());
    }
}
