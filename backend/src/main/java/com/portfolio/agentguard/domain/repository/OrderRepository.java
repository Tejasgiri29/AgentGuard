package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByTenantId(String tenantId);
    Optional<Order> findByIdAndTenantId(String id, String tenantId);
    Optional<Order> findByPurchaseIntentIdAndTenantId(String purchaseIntentId, String tenantId);
}
