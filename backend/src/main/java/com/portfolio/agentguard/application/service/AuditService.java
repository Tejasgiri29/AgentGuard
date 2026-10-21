package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.model.AuditEvent;
import com.portfolio.agentguard.domain.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Async
    public void record(String tenantId, String entityName, String entityId, String action, String actorId, String actorType, String snapshot) {
        AuditEvent event = AuditEvent.builder()
            .id("aud-" + UUID.randomUUID())
            .tenantId(tenantId)
            .entityName(entityName)
            .entityId(entityId)
            .action(action)
            .actorId(actorId)
            .actorType(actorType)
            .payloadSnapshot(snapshot)
            .build();
        auditEventRepository.save(event);
    }
}
