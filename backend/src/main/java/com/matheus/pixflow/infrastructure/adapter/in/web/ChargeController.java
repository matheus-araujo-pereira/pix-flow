package com.matheus.pixflow.infrastructure.adapter.in.web;

import com.matheus.pixflow.application.dto.ChargeResponse;
import com.matheus.pixflow.application.dto.CreateChargeRequest;
import com.matheus.pixflow.application.dto.PageResponse;
import com.matheus.pixflow.application.port.in.CancelChargeUseCase;
import com.matheus.pixflow.application.port.in.CreateChargeUseCase;
import com.matheus.pixflow.application.port.in.GetChargeUseCase;
import com.matheus.pixflow.application.port.in.SettleChargeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/charges")
@Tag(name = "PIX Charges", description = "Operations for creating, querying, settling and cancelling PIX transactions")
public class ChargeController {

    private final CreateChargeUseCase createChargeUseCase;
    private final GetChargeUseCase getChargeUseCase;
    private final CancelChargeUseCase cancelChargeUseCase;
    private final SettleChargeUseCase settleChargeUseCase;

    public ChargeController(
            CreateChargeUseCase createChargeUseCase,
            GetChargeUseCase getChargeUseCase,
            CancelChargeUseCase cancelChargeUseCase,
            SettleChargeUseCase settleChargeUseCase
    ) {
        this.createChargeUseCase = createChargeUseCase;
        this.getChargeUseCase = getChargeUseCase;
        this.cancelChargeUseCase = cancelChargeUseCase;
        this.settleChargeUseCase = settleChargeUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a PIX charge", description = "Generates a new PIX transaction, EMVCo BR Code, and dispatches it to the async settlement queue.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Charge created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error in payload"),
            @ApiResponse(responseCode = "422", description = "Business validation rule violation")
    })
    public ResponseEntity<ChargeResponse> createCharge(@Valid @RequestBody CreateChargeRequest request) {
        ChargeResponse response = createChargeUseCase.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "List charges", description = "Retrieves a paginated list of PIX charges sorted by creation date descending.")
    @ApiResponse(responseCode = "200", description = "Paginated list retrieved")
    public ResponseEntity<PageResponse<ChargeResponse>> listCharges(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1 to 100)") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ChargeResponse> response = getChargeUseCase.listAll(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get charge by ID", description = "Retrieves details of a specific charge using its internal UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Charge found"),
            @ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ChargeResponse> getChargeById(@PathVariable UUID id) {
        ChargeResponse response = getChargeUseCase.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tx/{txId}")
    @Operation(summary = "Get charge by TxId", description = "Retrieves details of a specific charge using its public PIX TxId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Charge found"),
            @ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ChargeResponse> getChargeByTxId(@PathVariable String txId) {
        ChargeResponse response = getChargeUseCase.getByTxId(txId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending charge", description = "Cancels a charge that is currently in PENDING status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Charge cancelled"),
            @ApiResponse(responseCode = "404", description = "Charge not found"),
            @ApiResponse(responseCode = "409", description = "Charge cannot be cancelled from its current status")
    })
    public ResponseEntity<ChargeResponse> cancelCharge(@PathVariable UUID id) {
        ChargeResponse response = cancelChargeUseCase.cancel(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/settle")
    @Operation(summary = "Trigger manual settlement simulation", description = "Manually triggers the clearing process for a pending transaction (useful for live demonstrations and testing).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settlement executed"),
            @ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ChargeResponse> forceSettle(@PathVariable UUID id) {
        ChargeResponse response = settleChargeUseCase.triggerSettlement(id);
        return ResponseEntity.ok(response);
    }
}

