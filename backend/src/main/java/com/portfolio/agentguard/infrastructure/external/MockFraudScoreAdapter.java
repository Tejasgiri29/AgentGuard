package com.portfolio.agentguard.infrastructure.external;

import com.portfolio.agentguard.application.port.FraudScorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class MockFraudScoreAdapter implements FraudScorePort {

    @Override
    public int predictAnomalyScore(String tenantId, String agentId, String merchant, BigDecimal amount) {
        log.info("[MockMLModel] Predicting anomaly probability for Merchant: {}, Amount: ${}", merchant, amount);
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            return 65;
        }
        return 12;
    }
}
