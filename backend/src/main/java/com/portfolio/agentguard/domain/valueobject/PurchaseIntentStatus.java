package com.portfolio.agentguard.domain.valueobject;

public enum PurchaseIntentStatus {
    CREATED,
    EVALUATING,
    AWAITING_HUMAN_APPROVAL,
    APPROVED,
    ORDER_RESERVED,
    PAYMENT_CAPTURED,
    LEDGER_POSTED,
    COMPLETED,
    DECLINED,
    FAILED,
    REFUNDED
}
