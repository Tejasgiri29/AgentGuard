package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.application.service.SubscriptionService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.Invoice;
import com.portfolio.agentguard.domain.model.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions & Dunning", description = "Recurring billing, invoices, and automated dunning lifecycle")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "List subscriptions")
    public ResponseEntity<List<Subscription>> listSubscriptions() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.listSubscriptions(tenantId));
    }

    @GetMapping("/invoices")
    @Operation(summary = "List invoices")
    public ResponseEntity<List<Invoice>> listInvoices() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.listInvoices(tenantId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel active subscription")
    public ResponseEntity<Subscription> cancelSubscription(@PathVariable String id) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.cancelSubscription(tenantId, id));
    }
}
