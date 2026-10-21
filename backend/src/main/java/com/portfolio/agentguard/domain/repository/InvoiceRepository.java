package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    List<Invoice> findByTenantIdAndSubscriptionId(String tenantId, String subscriptionId);
    Optional<Invoice> findByIdAndTenantId(String id, String tenantId);
}
