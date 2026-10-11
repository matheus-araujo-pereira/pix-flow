package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.domain.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<SettlementAuditLogJpaEntity, UUID> {

    List<SettlementAuditLogJpaEntity> findByChargeIdOrderByCreatedAtAsc(UUID chargeId);

    @Query("SELECT AVG(a.latencyMs) FROM SettlementAuditLogJpaEntity a WHERE a.newStatus = :status AND a.latencyMs > 0")
    Double calculateAverageLatencyByStatus(TransactionStatus status);
}

