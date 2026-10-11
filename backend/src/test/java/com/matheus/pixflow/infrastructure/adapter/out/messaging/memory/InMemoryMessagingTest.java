package com.matheus.pixflow.infrastructure.adapter.out.messaging.memory;

import com.matheus.pixflow.application.dto.SettlementMessage;
import com.matheus.pixflow.application.service.SettlementEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryMessagingTest {

    @Mock
    private SettlementEngineService settlementEngineService;

    @Test
    @DisplayName("Should publish message to in-memory queue and execute worker processing")
    void shouldPublishAndProcess() throws Exception {
        InMemoryChargeEventPublisher publisher = new InMemoryChargeEventPublisher();
        InMemorySettlementWorker worker = new InMemorySettlementWorker(publisher, settlementEngineService);

        assertFalse(worker.isRunning());
        worker.start();
        assertTrue(worker.isRunning());

        UUID chargeId = UUID.randomUUID();
        SettlementMessage message = SettlementMessage.of(chargeId, "tx123", new BigDecimal("100.00"));

        publisher.publish(message);

        // Allow worker loop to dequeue and process
        Thread.sleep(600);

        verify(settlementEngineService, atLeastOnce()).processSettlement(eq(chargeId), anyString());

        worker.stop();
        assertFalse(worker.isRunning());
    }

    @Test
    @DisplayName("Should handle multiple start and stop calls idempotently")
    void shouldHandleLifecycleIdempotently() {
        InMemoryChargeEventPublisher publisher = new InMemoryChargeEventPublisher();
        InMemorySettlementWorker worker = new InMemorySettlementWorker(publisher, settlementEngineService);

        worker.stop(); // stop before start when workerExecutor is null

        worker.start();
        worker.start();
        assertTrue(worker.isRunning());

        worker.stop();
        worker.stop();
        assertFalse(worker.isRunning());
    }

    @Test
    @DisplayName("Should handle processing exception in worker without crashing")
    void shouldHandleWorkerException() throws Exception {
        InMemoryChargeEventPublisher publisher = new InMemoryChargeEventPublisher();
        InMemorySettlementWorker worker = new InMemorySettlementWorker(publisher, settlementEngineService);

        doThrow(new RuntimeException("Simulated worker error"))
                .when(settlementEngineService).processSettlement(any(), any());

        worker.start();
        UUID chargeId = UUID.randomUUID();
        publisher.publish(SettlementMessage.of(chargeId, "tx1", BigDecimal.TEN));

        Thread.sleep(300);
        worker.stop();
    }

    @Test
    @DisplayName("Should handle poll timeout when queue is empty")
    void shouldHandlePollTimeout() throws Exception {
        InMemoryChargeEventPublisher publisher = new InMemoryChargeEventPublisher();
        InMemorySettlementWorker worker = new InMemorySettlementWorker(publisher, settlementEngineService);

        worker.start();
        // Wait 150ms for poll(100ms) to time out and return null
        Thread.sleep(150);
        worker.stop();

        verify(settlementEngineService, never()).processSettlement(any(), any());
    }
}

