package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.port.out.SettlementAuditPort;
import com.matheus.pixflow.domain.exception.ChargeNotFoundException;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.domain.repository.ChargeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementEngineServiceTest {

    @Mock
    private ChargeRepositoryPort chargeRepository;

    @Mock
    private SettlementAuditPort auditPort;

    private SettlementEngineService engineService;

    @BeforeEach
    void setUp() {
        // Zero delay for fast deterministic tests
        engineService = new SettlementEngineService(
                chargeRepository,
                auditPort,
                0L,
                0L,
                new BigDecimal("50000.00")
        );
    }

    @Test
    @DisplayName("Should successfully settle standard transaction")
    void shouldSettleTransaction() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        UUID id = charge.getId();

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeResponse response = engineService.processSettlement(id, "TEST-WORKER-01");

        assertNotNull(response);
        assertEquals(TransactionStatus.SETTLED, response.status());
        assertNotNull(response.settledAt());

        verify(auditPort, times(2)).recordTransition(eq(id), any(), any(), eq("TEST-WORKER-01"), anyLong(), anyString());
    }

    @Test
    @DisplayName("Should fail transaction when amount exceeds failure threshold")
    void shouldFailTransactionExceedingThreshold() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("60000.00"), "High value");
        UUID id = charge.getId();

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeResponse response = engineService.processSettlement(id, "TEST-WORKER-01");

        assertNotNull(response);
        assertEquals(TransactionStatus.FAILED, response.status());
        assertTrue(response.failureReason().contains("Risk rejection"));
    }

    @Test
    @DisplayName("Should skip processing if transaction is already in terminal state")
    void shouldSkipAlreadyTerminalTransaction() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        charge.transitionToProcessing();
        charge.settle();
        UUID id = charge.getId();

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));

        ChargeResponse response = engineService.processSettlement(id, "TEST-WORKER-01");

        assertEquals(TransactionStatus.SETTLED, response.status());
        verify(chargeRepository, never()).save(any(Charge.class));
        verify(auditPort, never()).recordTransition(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when charge ID does not exist")
    void shouldThrowWhenChargeNotFound() {
        UUID id = UUID.randomUUID();
        when(chargeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ChargeNotFoundException.class, () -> engineService.processSettlement(id, "TEST-WORKER-01"));
    }

    @Test
    @DisplayName("Should trigger manual settlement")
    void shouldTriggerManualSettlement() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        UUID id = charge.getId();

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeResponse response = engineService.triggerSettlement(id);

        assertEquals(TransactionStatus.SETTLED, response.status());
    }
}
