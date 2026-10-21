package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, String> {
    List<Mandate> findByTenantId(String tenantId);
    Optional<Mandate> findByIdAndTenantId(String id, String tenantId);
    List<Mandate> findByTenantIdAndAgentId(String tenantId, String agentId);
}
