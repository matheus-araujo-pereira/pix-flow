package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.application.port.out.SettlementAuditPort;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.domain.repository.ChargeRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ChargePersistenceAdapter implements ChargeRepositoryPort, SettlementAuditPort {

    private final SpringDataChargeRepository chargeRepository;
    private final SpringDataAuditLogRepository auditLogRepository;

    public ChargePersistenceAdapter(
            SpringDataChargeRepository chargeRepository,
            SpringDataAuditLogRepository auditLogRepository
    ) {
        this.chargeRepository = chargeRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Charge save(Charge charge) {
        ChargeJpaEntity entity = ChargeJpaEntity.fromDomain(charge);
        ChargeJpaEntity saved = chargeRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Charge> findById(UUID id) {
        return chargeRepository.findById(id).map(ChargeJpaEntity::toDomain);
    }

    @Override
    public Optional<Charge> findByTxId(String txId) {
        return chargeRepository.findByTxId(txId).map(ChargeJpaEntity::toDomain);
    }

    @Override
    public List<Charge> findAll(int page, int size) {
        return chargeRepository.findAllPaged(PageRequest.of(page, size))
                .stream()
                .map(ChargeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countTotal() {
        return chargeRepository.count();
    }

    @Override
    public long countByStatus(TransactionStatus status) {
        return chargeRepository.countByStatus(status);
    }

    @Override
    public BigDecimal sumSettledAmount() {
        BigDecimal sum = chargeRepository.sumAmountByStatus(TransactionStatus.SETTLED);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public Double calculateAverageSettlementDurationMs() {
        Double avg = auditLogRepository.calculateAverageLatencyByStatus(TransactionStatus.SETTLED);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    public void recordTransition(
            UUID chargeId,
            TransactionStatus previousStatus,
            TransactionStatus newStatus,
            String workerId,
            Long latencyMs,
            String details
    ) {
        SettlementAuditLogJpaEntity auditEntity = SettlementAuditLogJpaEntity.create(
                chargeId,
                previousStatus,
                newStatus,
                workerId,
                latencyMs,
                details
        );
        auditLogRepository.save(auditEntity);
    }

    @Override
    public List<SettlementAuditLogJpaEntity> findLogsByChargeId(UUID chargeId) {
        return auditLogRepository.findByChargeIdOrderByCreatedAtAsc(chargeId);
    }
}

