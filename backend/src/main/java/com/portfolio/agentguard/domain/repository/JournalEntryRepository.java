package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {
    List<JournalEntry> findByTenantIdOrderByPostedAtDesc(String tenantId);
    Optional<JournalEntry> findByIdAndTenantId(String id, String tenantId);
    List<JournalEntry> findByTenantIdAndReferenceId(String tenantId, String referenceId);
}
