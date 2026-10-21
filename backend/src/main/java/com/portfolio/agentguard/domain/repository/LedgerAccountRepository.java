package com.portfolio.agentguard.domain.repository;

import com.portfolio.agentguard.domain.model.LedgerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, String> {
    List<LedgerAccount> findByTenantId(String tenantId);
    Optional<LedgerAccount> findByIdAndTenantId(String id, String tenantId);
    Optional<LedgerAccount> findByTenantIdAndAccountNumber(String tenantId, String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LedgerAccount a WHERE a.id = :id AND a.tenantId = :tenantId")
    Optional<LedgerAccount> findWithLockByIdAndTenantId(@Param("id") String id, @Param("tenantId") String tenantId);
}
