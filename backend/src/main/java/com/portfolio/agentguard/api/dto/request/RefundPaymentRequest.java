package com.portfolio.agentguard.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundPaymentRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    String reason
) {}
