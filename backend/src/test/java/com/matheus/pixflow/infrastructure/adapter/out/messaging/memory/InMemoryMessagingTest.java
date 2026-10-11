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
}

