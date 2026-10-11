package com.matheus.pixflow.domain.repository;

import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepositoryPort {
    Charge save(Charge charge);
    Optional<Charge> findById(UUID id);
    Optional<Charge> findByTxId(String txId);
    List<Charge> findAll(int page, int size);
    long countTotal();
    long countByStatus(TransactionStatus status);
    BigDecimal sumSettledAmount();
    Double calculateAverageSettlementDurationMs();
}
