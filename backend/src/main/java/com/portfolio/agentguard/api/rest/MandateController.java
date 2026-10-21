package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.request.CreateMandateRequest;
import com.portfolio.agentguard.application.service.MandateService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.Mandate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mandates")
@RequiredArgsConstructor
@Tag(name = "Mandates", description = "Agent spending boundary and policy administration")
public class MandateController {

    private final MandateService mandateService;

    @GetMapping
    @Operation(summary = "List all active mandates for tenant")
    public ResponseEntity<List<Mandate>> listMandates() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(mandateService.listMandates(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mandate details")
    public ResponseEntity<Mandate> getMandate(@PathVariable String id) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(mandateService.getMandate(tenantId, id));
    }

    @PostMapping
    @Operation(summary = "Create an agent mandate")
    public ResponseEntity<Mandate> createMandate(@Valid @RequestBody CreateMandateRequest request) {
        String tenantId = TenantContext.getTenantId();
        Mandate mandate = Mandate.builder()
            .userId(request.userId())
            .agentId(request.agentId())
            .name(request.name())
            .description(request.description())
            .maxSingleTransactionAmount(request.maxSingleTransactionAmount())
            .maxDailyAmount(request.maxDailyAmount())
            .maxMonthlyAmount(request.maxMonthlyAmount())
            .allowedCategories(request.allowedCategories())
            .blockedMerchants(request.blockedMerchants())
            .allowedMerchants(request.allowedMerchants())
            .requireApprovalAboveAmount(request.requireApprovalAboveAmount())
            .allowRecurringSubscriptions(request.allowRecurringSubscriptions())
            .validFrom(request.validFrom() != null ? request.validFrom() : Instant.now())
            .validTo(request.validTo() != null ? request.validTo() : Instant.now().plus(365, ChronoUnit.DAYS))
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(mandateService.createMandate(tenantId, mandate));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke spending authority on a mandate")
    public ResponseEntity<Mandate> revokeMandate(
        @PathVariable String id,
        @RequestParam(defaultValue = "Revoked by operator") String reason
    ) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(mandateService.revokeMandate(tenantId, id, reason));
    }
}
