package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.application.dto.BatchChargeRequest;
import com.matheus.pixflow.application.dto.BatchChargeResponse;
import com.matheus.pixflow.application.service.BatchChargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/charges/batch")
@Tag(name = "Laboratório de Carga (Batch)", description = "Disparo concorrente de cobranças em lote para testes de carga e concorrência")
public class BatchChargeController {

    private final BatchChargeService batchChargeService;

    public BatchChargeController(BatchChargeService batchChargeService) {
        this.batchChargeService = batchChargeService;
    }

    @PostMapping
    @Operation(summary = "Disparar lote concorrente de cobranças", description = "Emite de 1 a 20 cobranças em paralelo via Virtual/Async Threads para avaliar o comportamento do motor sob concorrência.")
    @ApiResponse(responseCode = "200", description = "Lote emitido com sucesso")
    public ResponseEntity<BatchChargeResponse> executeBatch(@Valid @RequestBody BatchChargeRequest request) {
        BatchChargeResponse response = batchChargeService.executeBatch(request);
        return ResponseEntity.ok(response);
    }
}

