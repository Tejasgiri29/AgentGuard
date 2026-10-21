package com.portfolio.agentguard.domain.model;

import com.portfolio.agentguard.domain.valueobject.SagaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "saga_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaExecution {

    @Id
    @Column(name = "saga_id", length = 64)
    private String sagaId;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "saga_type", nullable = false, length = 64)
    private String sagaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SagaStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "current_step", nullable = false, length = 64)
    private String currentStep;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (status == null) status = SagaStatus.STARTED;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }
}
