package com.matheus.pixflow.infrastructure.adapter.out.persistence;

import com.matheus.pixflow.domain.model.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataChargeRepository extends JpaRepository<ChargeJpaEntity, UUID> {

    Optional<ChargeJpaEntity> findByTxId(String txId);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM ChargeJpaEntity c WHERE c.status = :status")
    BigDecimal sumAmountByStatus(TransactionStatus status);

    @Query("SELECT c FROM ChargeJpaEntity c ORDER BY c.createdAt DESC")
    List<ChargeJpaEntity> findAllPaged(Pageable pageable);
}
