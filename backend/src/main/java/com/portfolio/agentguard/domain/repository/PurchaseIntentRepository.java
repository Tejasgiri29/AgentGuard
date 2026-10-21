package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.PurchaseIntent;
import com.portfolio.agentguard.domain.valueobject.PurchaseIntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseIntentRepository extends JpaRepository<PurchaseIntent, String> {
    List<PurchaseIntent> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<PurchaseIntent> findByIdAndTenantId(String id, String tenantId);
    List<PurchaseIntent> findByTenantIdAndStatus(String tenantId, PurchaseIntentStatus status);

    @Query("SELECT COUNT(p) FROM PurchaseIntent p WHERE p.tenantId = :tenantId AND p.agentId = :agentId AND p.createdAt >= :since")
    int countRecentIntentsByAgent(@Param("tenantId") String tenantId,
                                  @Param("agentId") String agentId,
                                  @Param("since") Instant since);
}
