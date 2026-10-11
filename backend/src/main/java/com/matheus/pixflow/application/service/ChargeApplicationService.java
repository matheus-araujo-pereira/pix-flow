package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.*;
import com.matheus.pixflow.application.port.in.CancelChargeUseCase;
import com.matheus.pixflow.application.port.in.CreateChargeUseCase;
import com.matheus.pixflow.application.port.in.GetChargeUseCase;
import com.matheus.pixflow.application.port.in.GetMetricsUseCase;
import com.matheus.pixflow.application.port.out.ChargeEventPublisherPort;
import com.matheus.pixflow.application.port.out.SettlementAuditPort;
import com.matheus.pixflow.domain.exception.ChargeNotFoundException;
import com.matheus.pixflow.domain.model.Charge;
import com.matheus.pixflow.domain.model.TransactionStatus;
import com.matheus.pixflow.domain.repository.ChargeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChargeApplicationService implements CreateChargeUseCase, GetChargeUseCase, CancelChargeUseCase, GetMetricsUseCase {

    private final ChargeRepositoryPort chargeRepository;
    private final ChargeEventPublisherPort eventPublisher;
    private final SettlementAuditPort auditPort;

    public ChargeApplicationService(
            ChargeRepositoryPort chargeRepository,
            ChargeEventPublisherPort eventPublisher,
            SettlementAuditPort auditPort
    ) {
        this.chargeRepository = chargeRepository;
        this.eventPublisher = eventPublisher;
        this.auditPort = auditPort;
    }

    @Override
    public ChargeResponse create(CreateChargeRequest request) {
        Charge charge = Charge.create(
                request.pixKey(),
                request.pixKeyType(),
                request.amount(),
                request.description()
        );

        Charge saved = chargeRepository.save(charge);

        auditPort.recordTransition(
                saved.getId(),
                null,
                TransactionStatus.PENDING,
                "API_GATEWAY",
                0L,
                "Charge created and dispatched for asynchronous settlement."
        );

        eventPublisher.publish(SettlementMessage.of(saved.getId(), saved.getTxId(), saved.getAmount()));

        return ChargeResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargeResponse getById(UUID id) {
        return chargeRepository.findById(id)
                .map(ChargeResponse::fromDomain)
                .orElseThrow(() -> new ChargeNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ChargeResponse getByTxId(String txId) {
        return chargeRepository.findByTxId(txId)
                .map(ChargeResponse::fromDomain)
                .orElseThrow(() -> new ChargeNotFoundException(txId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChargeResponse> listAll(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));

        List<ChargeResponse> charges = chargeRepository.findAll(safePage, safeSize)
                .stream()
                .map(ChargeResponse::fromDomain)
                .toList();

        long totalElements = chargeRepository.countTotal();
        return PageResponse.of(charges, safePage, safeSize, totalElements);
    }

    @Override
    public ChargeResponse cancel(UUID id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ChargeNotFoundException(id));

        TransactionStatus previousStatus = charge.getStatus();
        charge.cancel();
        Charge saved = chargeRepository.save(charge);

        auditPort.recordTransition(
                saved.getId(),
                previousStatus,
                TransactionStatus.CANCELLED,
                "API_USER",
                0L,
                "Charge cancelled by client request."
        );

        return ChargeResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MetricsSummaryResponse getMetrics() {
        long total = chargeRepository.countTotal();
        long settled = chargeRepository.countByStatus(TransactionStatus.SETTLED);
        long pending = chargeRepository.countByStatus(TransactionStatus.PENDING);
        long processing = chargeRepository.countByStatus(TransactionStatus.PROCESSING);
        long failed = chargeRepository.countByStatus(TransactionStatus.FAILED);
        long cancelled = chargeRepository.countByStatus(TransactionStatus.CANCELLED);
        BigDecimal volume = chargeRepository.sumSettledAmount();
        Double avgDuration = chargeRepository.calculateAverageSettlementDurationMs();

        double successRate = 100.0;
        long finishedCount = settled + failed;
        if (finishedCount > 0) {
            successRate = BigDecimal.valueOf((double) settled / (double) finishedCount * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new MetricsSummaryResponse(
                total,
                settled,
                pending,
                processing,
                failed,
                cancelled,
                volume,
                avgDuration,
                successRate,
                "OPERATIONAL"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargeAuditLogResponse> getTimeline(UUID id) {
        // Valida existência da cobrança
        if (chargeRepository.findById(id).isEmpty()) {
            throw new ChargeNotFoundException(id);
        }

        return auditPort.findLogsByChargeId(id)
                .stream()
                .map(ChargeAuditLogResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportCsv() {
        List<Charge> charges = chargeRepository.findAll(0, 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("id,tx_id,end_to_end_id,pix_key,pix_key_type,amount,status,created_at,settled_at\n");

        for (Charge c : charges) {
            sb.append(c.getId()).append(",")
              .append(c.getTxId()).append(",")
              .append(c.getEndToEndId()).append(",")
              .append(c.getPixKey()).append(",")
              .append(c.getPixKeyType()).append(",")
              .append(c.getAmount()).append(",")
              .append(c.getStatus()).append(",")
              .append(c.getCreatedAt()).append(",")
              .append(c.getSettledAt() != null ? c.getSettledAt() : "").append("\n");
        }
        return sb.toString();
    }
}

