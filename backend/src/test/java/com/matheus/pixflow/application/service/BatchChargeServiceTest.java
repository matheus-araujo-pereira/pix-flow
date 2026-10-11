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

    // Direct synchronous executor for test deterministic execution
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
}
