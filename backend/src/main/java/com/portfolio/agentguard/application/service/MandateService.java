package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.Mandate;
import com.portfolio.agentguard.domain.repository.MandateRepository;
import com.portfolio.agentguard.domain.valueobject.MandateStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MandateService {

    private final MandateRepository mandateRepository;
    private final AuditService auditService;

    public List<Mandate> listMandates(String tenantId) {
        return mandateRepository.findByTenantId(tenantId);
    }

    public Mandate getMandate(String tenantId, String mandateId) {
        return mandateRepository.findByIdAndTenantId(mandateId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Mandate not found: " + mandateId));
    }

    @Transactional
    public Mandate createMandate(String tenantId, Mandate mandate) {
        mandate.setId("mnd-" + UUID.randomUUID());
        mandate.setTenantId(tenantId);
        mandate.setStatus(MandateStatus.ACTIVE);
        mandate.setVersion(1);
        Mandate saved = mandateRepository.save(mandate);
        auditService.record(tenantId, "MANDATE", saved.getId(), "CREATED", mandate.getUserId(), "USER", "Mandate created with limit $" + mandate.getMaxSingleTransactionAmount());
        return saved;
    }

    @Transactional
    public Mandate revokeMandate(String tenantId, String mandateId, String reason) {
        Mandate mandate = getMandate(tenantId, mandateId);
        mandate.setStatus(MandateStatus.REVOKED);
        Mandate saved = mandateRepository.save(mandate);
        auditService.record(tenantId, "MANDATE", saved.getId(), "REVOKED", "ADMIN", "ADMIN", reason);
        return saved;
    }
}
