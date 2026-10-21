package com.portfolio.agentguard.api.dto.response;

import java.math.BigDecimal;

public record DashboardStatsResponse(
    long totalIntents,
    long pendingApprovals,
    long activeMandates,
    BigDecimal totalSettledAmount,
    double paymentSuccessRate,
    long activeSubscriptions,
    boolean ledgerBalanced
) {}
