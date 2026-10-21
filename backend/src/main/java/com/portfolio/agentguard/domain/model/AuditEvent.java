package com.portfolio.agentguard.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "entity_name", nullable = false, length = 128)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "actor_id", nullable = false, length = 64)
    private String actorId;

    @Column(name = "actor_type", nullable = false, length = 32)
    private String actorType; // USER, AGENT, SYSTEM, ADMIN

    @Column(name = "payload_snapshot", columnDefinition = "TEXT")
    private String payloadSnapshot;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    public void onPrePersist() {
        if (timestamp == null) timestamp = Instant.now();
    }
}
