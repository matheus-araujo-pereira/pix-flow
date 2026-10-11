package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.port.in.SettleChargeUseCase;
import com.matheus.pixflow.application.port.out.SettlementAuditPort;
import com.matheus.pixflow.domain.exception.ChargeNotFoundException;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.domain.repository.ChargeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class SettlementEngineService implements SettleChargeUseCase {

    private static final Logger log = LoggerFactory.getLogger(SettlementEngineService.class);

    private final ChargeRepositoryPort chargeRepository;
    private final SettlementAuditPort auditPort;

    private final long minDelayMs;
    private final long maxDelayMs;
    private final BigDecimal failureThreshold;

    public SettlementEngineService(
            ChargeRepositoryPort chargeRepository,
            SettlementAuditPort auditPort,
            @Value("${pixflow.settlement.simulation-delay-min-ms:600}") long minDelayMs,
            @Value("${pixflow.settlement.simulation-delay-max-ms:1500}") long maxDelayMs,
            @Value("${pixflow.settlement.failure-threshold-amount:50000.00}") BigDecimal failureThreshold
    ) {
        this.chargeRepository = chargeRepository;
        this.auditPort = auditPort;
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.failureThreshold = failureThreshold;
    }

    @Override
    public ChargeResponse triggerSettlement(UUID id) {
        return processSettlement(id, "MANUAL_TRIGGER");
    }

    public ChargeResponse processSettlement(UUID id, String workerId) {
        long startTime = System.currentTimeMillis();

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ChargeNotFoundException(id));

        if (charge.getStatus().isTerminal()) {
            log.info("Charge {} is already in terminal state {}. Skipping settlement.", id, charge.getStatus());
            return ChargeResponse.fromDomain(charge);
        }

        TransactionStatus initialStatus = charge.getStatus();
        charge.transitionToProcessing();
        charge = chargeRepository.save(charge);

        auditPort.recordTransition(
                id,
                initialStatus,
                TransactionStatus.PROCESSING,
                workerId,
                System.currentTimeMillis() - startTime,
                "Transaction picked up by worker and sent to BACEN clearing queue."
        );

        // Simulate network clearing latency
        simulateClearingLatency();

        long processingDuration = System.currentTimeMillis() - startTime;

        if (charge.getAmount().compareTo(failureThreshold) > 0) {
            String reason = String.format("Risk rejection: amount R$ %s exceeds automated limit R$ %s",
                    charge.getAmount(), failureThreshold);
            charge.fail(reason);
            log.warn("Charge {} failed: {}", id, reason);
        } else {
            charge.settle();
            log.info("Charge {} successfully settled in {} ms by worker {}", id, processingDuration, workerId);
        }

        Charge finishedCharge = chargeRepository.save(charge);

        auditPort.recordTransition(
                id,
                TransactionStatus.PROCESSING,
                finishedCharge.getStatus(),
                workerId,
                processingDuration,
                finishedCharge.getStatus() == TransactionStatus.SETTLED
                        ? "Settlement confirmed by Central Bank SPI clearing network."
                        : finishedCharge.getFailureReason()
        );

        return ChargeResponse.fromDomain(finishedCharge);
    }

    private void simulateClearingLatency() {
        if (minDelayMs <= 0 || maxDelayMs <= 0) return;
        try {
            long delay = ThreadLocalRandom.current().nextLong(minDelayMs, Math.max(minDelayMs + 1, maxDelayMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Settlement simulation sleep interrupted");
        }
    }
}
