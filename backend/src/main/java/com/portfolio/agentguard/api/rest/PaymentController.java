package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.request.RefundPaymentRequest;
import com.portfolio.agentguard.application.service.PaymentService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.Payment;
import com.portfolio.agentguard.domain.model.Refund;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments & Settlement", description = "Multi-provider payment orchestration and refunds")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "List transactions")
    public ResponseEntity<List<Payment>> listPayments() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(paymentService.listPayments(tenantId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(paymentService.getPayment(tenantId, id));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Issue a partial or full refund")
    public ResponseEntity<Refund> issueRefund(
        @PathVariable String id,
        @Valid @RequestBody RefundPaymentRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(paymentService.issueRefund(tenantId, id, request.amount(), request.reason()));
    }
}
