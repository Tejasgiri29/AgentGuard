package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String> {
    List<AuditEvent> findByTenantIdOrderByTimestampDesc(String tenantId);
    List<AuditEvent> findByTenantIdAndEntityNameOrderByTimestampDesc(String tenantId, String entityName);
}
