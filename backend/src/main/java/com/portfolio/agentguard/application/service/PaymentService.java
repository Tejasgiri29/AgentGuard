package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.application.port.PaymentGatewayPort;
import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.Payment;
import com.portfolio.agentguard.domain.model.Refund;
import com.portfolio.agentguard.domain.repository.PaymentRepository;
import com.portfolio.agentguard.domain.repository.RefundRepository;
import com.portfolio.agentguard.domain.valueobject.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final AuditService auditService;

    public List<Payment> listPayments(String tenantId) {
        return paymentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public Payment getPayment(String tenantId, String paymentId) {
        return paymentRepository.findByIdAndTenantId(paymentId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
    }

    @Transactional
    public Refund issueRefund(String tenantId, String paymentId, BigDecimal amount, String reason) {
        Payment payment = getPayment(tenantId, paymentId);
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Cannot refund payment with status: " + payment.getStatus());
        }

        PaymentGatewayPort.PaymentResult result = paymentGatewayPort.refund(
            tenantId, payment.getProviderTransactionId(), amount, payment.getCurrency()
        );

        Refund refund = Refund.builder()
            .id("ref-" + UUID.randomUUID())
            .tenantId(tenantId)
            .paymentId(payment.getId())
            .amount(amount)
            .currency(payment.getCurrency())
            .status(result.success() ? "COMPLETED" : "FAILED")
            .reason(reason)
            .build();
        refundRepository.save(refund);

        if (result.success()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            auditService.record(tenantId, "REFUND", refund.getId(), "PROCESSED", "OPERATOR", "USER", "Refunded $" + amount);
        }

        return refund;
    }
}
