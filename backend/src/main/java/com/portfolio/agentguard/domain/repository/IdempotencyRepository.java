package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, IdempotencyRecord.IdempotencyKeyId> {
    Optional<IdempotencyRecord> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);
}
