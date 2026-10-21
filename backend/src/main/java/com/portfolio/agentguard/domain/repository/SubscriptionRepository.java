package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.Subscription;
import com.portfolio.agentguard.domain.valueobject.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    List<Subscription> findByTenantId(String tenantId);
    Optional<Subscription> findByIdAndTenantId(String id, String tenantId);
    List<Subscription> findByTenantIdAndStatus(String tenantId, SubscriptionStatus status);
}
