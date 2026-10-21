package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.request.CreatePurchaseIntentRequest;
import com.portfolio.agentguard.application.service.PurchaseIntentService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.PurchaseIntent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-intents")
@RequiredArgsConstructor
@Tag(name = "Purchase Intents", description = "AI-Agent spend submission and evaluation endpoints")
public class PurchaseIntentController {

    private final PurchaseIntentService purchaseIntentService;

    @PostMapping
    @Operation(summary = "Submit a purchase intent", description = "Receives agent spend intent, checks velocity and mandate limits, and runs risk engine")
    public ResponseEntity<PurchaseIntent> createPurchaseIntent(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreatePurchaseIntentRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        PurchaseIntent intent = purchaseIntentService.createPurchaseIntent(tenantId, new PurchaseIntentService.CreateIntentCommand(
            request.mandateId(),
            request.agentId(),
            request.merchantName(),
            request.merchantCategory(),
            request.amount(),
            request.currency(),
            request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(intent);
    }

    @GetMapping
    @Operation(summary = "List purchase intents", description = "Retrieves all purchase intents in the current tenant partition")
    public ResponseEntity<List<PurchaseIntent>> listPurchaseIntents() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(purchaseIntentService.listIntents(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase intent by ID")
    public ResponseEntity<PurchaseIntent> getPurchaseIntent(@PathVariable String id) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(purchaseIntentService.getIntent(tenantId, id));
    }
}
