package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.SagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, String> {
    Optional<SagaExecution> findBySagaIdAndTenantId(String sagaId, String tenantId);
}
