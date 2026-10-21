package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.RiskDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskDecisionRepository extends JpaRepository<RiskDecision, String> {
    Optional<RiskDecision> findByPurchaseIntentIdAndTenantId(String purchaseIntentId, String tenantId);
}
