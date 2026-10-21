package com.portfolio.agentguard.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePurchaseIntentRequest(
    @NotBlank String mandateId,
    @NotBlank String agentId,
    @NotBlank String merchantName,
    @NotBlank String merchantCategory,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    String currency,
    String description
) {}
