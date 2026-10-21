package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.response.DashboardStatsResponse;
import com.portfolio.agentguard.application.service.LedgerService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.Payment;
import com.portfolio.agentguard.domain.repository.ApprovalRepository;
import com.portfolio.agentguard.domain.repository.MandateRepository;
import com.portfolio.agentguard.domain.repository.PaymentRepository;
import com.portfolio.agentguard.domain.repository.PurchaseIntentRepository;
import com.portfolio.agentguard.domain.repository.SubscriptionRepository;
import com.portfolio.agentguard.domain.valueobject.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Administration", description = "Aggregated operational metrics and seed controls")
public class AdminSeedController {

    private final PurchaseIntentRepository purchaseIntentRepository;
    private final ApprovalRepository approvalRepository;
    private final MandateRepository mandateRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LedgerService ledgerService;

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get top-level dashboard metrics")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        String tenantId = TenantContext.getTenantId();

        long totalIntents = purchaseIntentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).size();
        long pendingApprovals = approvalRepository.findByTenantIdAndStatus(tenantId, "PENDING").size();
        long activeMandates = mandateRepository.findByTenantId(tenantId).size();
        List<Payment> payments = paymentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        BigDecimal settledTotal = payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long capturedCount = payments.stream().filter(p -> p.getStatus() == PaymentStatus.CAPTURED).count();
        double successRate = payments.isEmpty() ? 100.0 : ((double) capturedCount / payments.size()) * 100.0;
        long activeSubs = subscriptionRepository.findByTenantId(tenantId).size();
        boolean balanced = ledgerService.computeTrialBalance(tenantId).isBalanced();

        return ResponseEntity.ok(new DashboardStatsResponse(
            totalIntents,
            pendingApprovals,
            activeMandates,
            settledTotal,
            Math.round(successRate * 10.0) / 10.0,
            activeSubs,
            balanced
        ));
    }

    @PostMapping("/admin/seed")
    @Operation(summary = "Re-trigger demo seed confirmation")
    public ResponseEntity<Map<String, String>> seedDemoData() {
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "AgentGuard database is pre-seeded via Flyway V2 migration script."
        ));
    }
}
