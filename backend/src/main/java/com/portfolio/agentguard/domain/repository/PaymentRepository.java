package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<Payment> findByIdAndTenantId(String id, String tenantId);
    Optional<Payment> findByPurchaseIntentIdAndTenantId(String purchaseIntentId, String tenantId);
}
