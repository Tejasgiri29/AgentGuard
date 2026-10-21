package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.AuditEvent;
import com.portfolio.agentguard.domain.repository.AuditEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit/events")
@RequiredArgsConstructor
@Tag(name = "Audit & Compliance", description = "Immutable append-only audit trail export and querying")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    @GetMapping
    @Operation(summary = "List audit timeline events")
    public ResponseEntity<List<AuditEvent>> listAuditEvents() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(auditEventRepository.findByTenantIdOrderByTimestampDesc(tenantId));
    }
}
