package com.portfolio.agentguard.application.port;

import java.math.BigDecimal;

public interface PaymentGatewayPort {

    record PaymentResult(boolean success, String transactionId, String errorCode, String rawResponse) {}

    PaymentResult authorizeAndCapture(String tenantId, String intentId, BigDecimal amount, String currency, String merchant);
    PaymentResult refund(String tenantId, String originalTxId, BigDecimal amount, String currency);
}
