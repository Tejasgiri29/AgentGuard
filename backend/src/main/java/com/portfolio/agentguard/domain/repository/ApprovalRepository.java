package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, String> {
    List<Approval> findByTenantIdAndStatus(String tenantId, String status);
    Optional<Approval> findByIdAndTenantId(String id, String tenantId);
    Optional<Approval> findByPurchaseIntentIdAndTenantId(String purchaseIntentId, String tenantId);
}
