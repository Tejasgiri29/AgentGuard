package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {
    List<Refund> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<Refund> findByTenantIdAndPaymentId(String tenantId, String paymentId);
}
