package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.CreateChargeRequest;
import com.matheus.pixflow.application.dto.MetricsSummaryResponse;
import com.matheus.pixflow.application.dto.PageResponse;
import com.matheus.pixflow.application.port.out.ChargeEventPublisherPort;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeApplicationServiceTest {

    @Mock
    private ChargeRepositoryPort chargeRepository;

    @Mock
    private ChargeEventPublisherPort eventPublisher;

    @Mock
    private SettlementAuditPort auditPort;

    private ChargeApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ChargeApplicationService(chargeRepository, eventPublisher, auditPort);
    }

    @Test
    @DisplayName("Should successfully orchestrate charge creation")
    void shouldCreateCharge() {
        CreateChargeRequest request = new CreateChargeRequest(
                "dev@pixflow.com",
                PixKeyType.EMAIL,
                new BigDecimal("250.00"),
                "API Hosting"
        );

        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("dev@pixflow.com", response.pixKey());
        assertEquals(TransactionStatus.PENDING, response.status());

        verify(chargeRepository, times(1)).save(any(Charge.class));
        verify(auditPort, times(1)).recordTransition(
                eq(response.id()), isNull(), eq(TransactionStatus.PENDING), anyString(), eq(0L), anyString());
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("Should find charge by ID")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));

        ChargeResponse response = service.getById(id);
        assertNotNull(response);
        assertEquals(charge.getTxId(), response.txId());
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when ID not found")
    void shouldThrowWhenIdNotFound() {
        UUID id = UUID.randomUUID();
        when(chargeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ChargeNotFoundException.class, () -> service.getById(id));
    }

    @Test
    @DisplayName("Should find charge by TxId")
    void shouldFindByTxId() {
        String txId = "tx123456";
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");

        when(chargeRepository.findByTxId(txId)).thenReturn(Optional.of(charge));

        ChargeResponse response = service.getByTxId(txId);
        assertNotNull(response);
        assertEquals(charge.getTxId(), response.txId());
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when TxId not found")
    void shouldThrowWhenTxIdNotFound() {
        when(chargeRepository.findByTxId("unknown")).thenReturn(Optional.empty());
        assertThrows(ChargeNotFoundException.class, () -> service.getByTxId("unknown"));
    }

    @Test
    @DisplayName("Should list charges with pagination")
    void shouldListCharges() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        when(chargeRepository.findAll(0, 10)).thenReturn(List.of(charge));
        when(chargeRepository.countTotal()).thenReturn(1L);

        PageResponse<ChargeResponse> page = service.listAll(0, 10);
        assertNotNull(page);
        assertEquals(1, page.content().size());
        assertEquals(1, page.totalElements());
    }

    @Test
    @DisplayName("Should cancel pending charge")
    void shouldCancelCharge() {
        UUID id = UUID.randomUUID();
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");

        when(chargeRepository.findById(id)).thenReturn(Optional.of(charge));
        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeResponse response = service.cancel(id);
        assertEquals(TransactionStatus.CANCELLED, response.status());

        verify(auditPort, times(1)).recordTransition(
                eq(charge.getId()), eq(TransactionStatus.PENDING), eq(TransactionStatus.CANCELLED),
                eq("API_USER"), eq(0L), anyString());
    }

    @Test
    @DisplayName("Should calculate system metrics accurately")
    void shouldCalculateMetrics() {
        when(chargeRepository.countTotal()).thenReturn(10L);
        when(chargeRepository.countByStatus(TransactionStatus.SETTLED)).thenReturn(8L);
        when(chargeRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(1L);
        when(chargeRepository.countByStatus(TransactionStatus.PROCESSING)).thenReturn(0L);
        when(chargeRepository.countByStatus(TransactionStatus.FAILED)).thenReturn(1L);
        when(chargeRepository.countByStatus(TransactionStatus.CANCELLED)).thenReturn(0L);
        when(chargeRepository.sumSettledAmount()).thenReturn(new BigDecimal("1500.00"));
        when(chargeRepository.calculateAverageSettlementDurationMs()).thenReturn(950.4);

        MetricsSummaryResponse metrics = service.getMetrics();

        assertNotNull(metrics);
        assertEquals(10L, metrics.totalTransactions());
        assertEquals(8L, metrics.settledCount());
        assertEquals(new BigDecimal("1500.00"), metrics.totalSettledVolume());
        assertEquals(950.4, metrics.averageSettlementTimeMs());
        // 8 settled out of 9 finished (8+1) -> 88.9%
        assertEquals(88.9, metrics.successRatePercentage());
    }
}

