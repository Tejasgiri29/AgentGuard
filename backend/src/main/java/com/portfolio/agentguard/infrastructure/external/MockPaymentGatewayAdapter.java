package com.portfolio.agentguard.infrastructure.external;

import com.portfolio.agentguard.application.port.PaymentGatewayPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public PaymentResult authorizeAndCapture(String tenantId, String intentId, BigDecimal amount, String currency, String merchant) {
        log.info("[MockPaymentGateway] Authorizing and capturing ${} {} for merchant '{}' (Intent: {})",
            amount, currency, merchant, intentId);

        // Realistic mock logic: Amounts ending in .99 or high value trigger specific scenarios
        if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
            log.warn("[MockPaymentGateway] Simulated card limit exceeded");
            return new PaymentResult(false, null, "INSUFFICIENT_CREDIT_LIMIT", "{\"error\":\"insufficient_funds\"}");
        }

        String transactionId = "ch_mock_" + UUID.randomUUID().toString().substring(0, 12);
        return new PaymentResult(true, transactionId, null, "{\"status\":\"succeeded\",\"id\":\"" + transactionId + "\"}");
    }

    @Override
    public PaymentResult refund(String tenantId, String originalTxId, BigDecimal amount, String currency) {
        log.info("[MockPaymentGateway] Refunding ${} {} for transaction {}", amount, currency, originalTxId);
        String refundId = "re_mock_" + UUID.randomUUID().toString().substring(0, 12);
        return new PaymentResult(true, refundId, null, "{\"status\":\"refunded\",\"id\":\"" + refundId + "\"}");
    }
}
