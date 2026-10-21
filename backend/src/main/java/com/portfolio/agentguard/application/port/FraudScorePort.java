package com.portfolio.agentguard.application.port;

import java.math.BigDecimal;

public interface FraudScorePort {
    int predictAnomalyScore(String tenantId, String agentId, String merchant, BigDecimal amount);
}
