package com.portfolio.agentguard.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateMandateRequest(
    @NotBlank String userId,
    @NotBlank String agentId,
    @NotBlank String name,
    String description,
    @NotNull @DecimalMin("1.00") BigDecimal maxSingleTransactionAmount,
    @NotNull @DecimalMin("1.00") BigDecimal maxDailyAmount,
    @NotNull @DecimalMin("1.00") BigDecimal maxMonthlyAmount,
    String allowedCategories,
    String blockedMerchants,
    String allowedMerchants,
    @NotNull @DecimalMin("0.00") BigDecimal requireApprovalAboveAmount,
    boolean allowRecurringSubscriptions,
    Instant validFrom,
    Instant validTo
) {}
