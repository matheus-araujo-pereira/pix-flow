package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.application.dto.WebhookDeliveryResponse;
import com.matheus.pixflow.application.service.WebhookDispatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks & Notificações", description = "Monitoramento e verificação de webhooks de liquidação assinados com HMAC-SHA256")
public class WebhookController {

    private final WebhookDispatcherService webhookDispatcherService;

    public WebhookController(WebhookDispatcherService webhookDispatcherService) {
        this.webhookDispatcherService = webhookDispatcherService;
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Listar notificações de webhooks recentes", description = "Recupera o histórico de eventos de webhook despachados para os clientes externos.")
    @ApiResponse(responseCode = "200", description = "Lista de webhooks recuperada")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveries(
            @Parameter(description = "Limite de registros a retornar (1 a 50)") @RequestParam(defaultValue = "20") int limit
    ) {
        List<WebhookDeliveryResponse> deliveries = webhookDispatcherService.getRecentDeliveries(limit);
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/charges/{chargeId}")
    @Operation(summary = "Listar webhooks por ID de cobrança", description = "Recupera os webhooks enviados para uma cobrança específica.")
    public ResponseEntity<List<WebhookDeliveryResponse>> getDeliveriesByCharge(@PathVariable UUID chargeId) {
        return ResponseEntity.ok(webhookDispatcherService.getDeliveriesByCharge(chargeId));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verificar assinatura criptográfica HMAC-SHA256", description = "Valida a autenticidade de um payload de webhook utilizando a chave secreta do motor PixFlow.")
    public ResponseEntity<Map<String, Object>> verifySignature(
            @RequestBody Map<String, String> request
    ) {
        String payload = request.get("payload");
        String signature = request.get("signature");

        boolean isValid = webhookDispatcherService.verifySignature(payload, signature);
        return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "algorithm", "HmacSHA256"
        ));
    }
}
