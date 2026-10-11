package com.matheus.pixflow.application.service;

import com.matheus.pixflow.application.dto.BatchChargeRequest;
import com.matheus.pixflow.application.dto.BatchChargeResponse;
import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.CreateChargeRequest;
import com.matheus.pixflow.application.port.in.CreateChargeUseCase;
import com.matheus.pixflow.domain.model.PixKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class BatchChargeService {

    private static final Logger log = LoggerFactory.getLogger(BatchChargeService.class);

    private final CreateChargeUseCase createChargeUseCase;
    private final Executor pixflowTaskExecutor;

    public BatchChargeService(
            CreateChargeUseCase createChargeUseCase,
            Executor pixflowTaskExecutor
    ) {
        this.createChargeUseCase = createChargeUseCase;
        this.pixflowTaskExecutor = pixflowTaskExecutor;
    }

    public BatchChargeResponse executeBatch(BatchChargeRequest request) {
        long startTime = System.currentTimeMillis();
        int count = Math.max(1, Math.min(20, request.count()));
        BigDecimal amount = (request.amount() != null && request.amount().compareTo(BigDecimal.ZERO) > 0)
                ? request.amount()
                : new BigDecimal("50.00");

        PixKeyType keyType = request.pixKeyType() != null ? request.pixKeyType() : PixKeyType.EMAIL;
        String baseDesc = (request.baseDescription() != null && !request.baseDescription().isBlank())
                ? request.baseDescription().trim()
                : "Batch Load Test";

        log.info("Iniciando emissão concorrente de lote com {} transações (Valor R$ {})", count, amount);

        List<CompletableFuture<ChargeResponse>> futures = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            final int index = i;
            String key = generateKeyForBatch(keyType, index);
            String desc = String.format("%s [#%02d]", baseDesc, index);

            CompletableFuture<ChargeResponse> future = CompletableFuture.supplyAsync(() -> {
                CreateChargeRequest chargeReq = new CreateChargeRequest(key, keyType, amount, desc);
                return createChargeUseCase.create(chargeReq);
            }, pixflowTaskExecutor);

            futures.add(future);
        }

        List<ChargeResponse> results = Collections.synchronizedList(new ArrayList<>());
        for (CompletableFuture<ChargeResponse> future : futures) {
            try {
                results.add(future.join());
            } catch (Exception e) {
                log.error("Falha ao emitir transação do lote: {}", e.getMessage(), e);
            }
        }

        long totalDurationMs = System.currentTimeMillis() - startTime;
        log.info("Lote de {} transações emitido com sucesso em {} ms", results.size(), totalDurationMs);

        return new BatchChargeResponse(count, results.size(), totalDurationMs, results);
    }

    private String generateKeyForBatch(PixKeyType type, int index) {
        return switch (type) {
            case CPF -> "52998224725"; // CPF canônico válido
            case CNPJ -> "04252011000110"; // CNPJ canônico válido
            case PHONE -> "+551198765" + String.format("%04d", index);
            case RANDOM -> UUID.randomUUID().toString();
            case EMAIL -> String.format("stress-worker-%02d@pixflow.com.br", index);
        };
    }
}
