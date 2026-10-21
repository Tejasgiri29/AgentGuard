package com.portfolio.agentguard.domain.exception;

public class MandateViolationException extends RuntimeException {
    private final String reasonCode;

    public MandateViolationException(String message, String reasonCode) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
