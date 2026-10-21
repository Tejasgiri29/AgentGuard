package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.RiskRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, String> {
    List<RiskRule> findByTenantIdAndActiveTrue(String tenantId);
    List<RiskRule> findByTenantId(String tenantId);
}
