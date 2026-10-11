package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.PixKeyType;
import com.matheus.pixflow.domain.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargePersistenceAdapterTest {

    @Mock
    private SpringDataChargeRepository chargeRepository;

    @Mock
    private SpringDataAuditLogRepository auditLogRepository;

    private ChargePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ChargePersistenceAdapter(chargeRepository, auditLogRepository);
    }

    @Test
    @DisplayName("Should save domain charge through JPA repository")
    void shouldSaveCharge() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        ChargeJpaEntity entity = ChargeJpaEntity.fromDomain(charge);

        when(chargeRepository.save(any(ChargeJpaEntity.class))).thenReturn(entity);

        Charge saved = adapter.save(charge);
        assertNotNull(saved);
        assertEquals(charge.getId(), saved.getId());
    }

    @Test
    @DisplayName("Should find charge by ID")
    void shouldFindById() {
        UUID id = UUID.randomUUID();
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        ChargeJpaEntity entity = ChargeJpaEntity.fromDomain(charge);

        when(chargeRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Charge> result = adapter.findById(id);
        assertTrue(result.isPresent());
        assertEquals(charge.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Should find charge by TxId")
    void shouldFindByTxId() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        ChargeJpaEntity entity = ChargeJpaEntity.fromDomain(charge);

        when(chargeRepository.findByTxId("tx123")).thenReturn(Optional.of(entity));

        Optional<Charge> result = adapter.findByTxId("tx123");
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should find all paged")
    void shouldFindAllPaged() {
        Charge charge = Charge.create("dev@pixflow.com", PixKeyType.EMAIL, new BigDecimal("100.00"), "desc");
        ChargeJpaEntity entity = ChargeJpaEntity.fromDomain(charge);

        when(chargeRepository.findAllPaged(any(Pageable.class))).thenReturn(List.of(entity));

        List<Charge> list = adapter.findAll(0, 10);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("Should query counts, sum, and average latency")
    void shouldQueryMetrics() {
        when(chargeRepository.count()).thenReturn(100L);
        when(chargeRepository.countByStatus(TransactionStatus.SETTLED)).thenReturn(90L);
        when(chargeRepository.sumAmountByStatus(TransactionStatus.SETTLED)).thenReturn(new BigDecimal("5000.00"));
        when(auditLogRepository.calculateAverageLatencyByStatus(TransactionStatus.SETTLED)).thenReturn(750.45);

        assertEquals(100L, adapter.countTotal());
        assertEquals(90L, adapter.countByStatus(TransactionStatus.SETTLED));
        assertEquals(new BigDecimal("5000.00"), adapter.sumSettledAmount());
        assertEquals(750.5, adapter.calculateAverageSettlementDurationMs());
    }

    @Test
    @DisplayName("Should record audit transition log")
    void shouldRecordTransition() {
        UUID chargeId = UUID.randomUUID();
        adapter.recordTransition(
                chargeId,
                TransactionStatus.PENDING,
                TransactionStatus.PROCESSING,
                "WORKER-01",
                200L,
                "State transition"
        );

        verify(auditLogRepository, times(1)).save(any(SettlementAuditLogJpaEntity.class));
    }

    @Test
    @DisplayName("Should handle null sum and null average latency returning zero")
    void shouldHandleNullMetrics() {
        when(chargeRepository.sumAmountByStatus(TransactionStatus.SETTLED)).thenReturn(null);
        when(auditLogRepository.calculateAverageLatencyByStatus(TransactionStatus.SETTLED)).thenReturn(null);

        assertEquals(BigDecimal.ZERO, adapter.sumSettledAmount());
        assertEquals(0.0, adapter.calculateAverageSettlementDurationMs());
    }

    @Test
    @DisplayName("Should find audit logs by chargeId")
    void shouldFindLogsByChargeId() {
        UUID chargeId = UUID.randomUUID();
        when(auditLogRepository.findByChargeIdOrderByCreatedAtAsc(chargeId)).thenReturn(List.of());

        List<SettlementAuditLogJpaEntity> logs = adapter.findLogsByChargeId(chargeId);
        assertNotNull(logs);
        assertTrue(logs.isEmpty());
    }
}

