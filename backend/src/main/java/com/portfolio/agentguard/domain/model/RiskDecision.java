package com.portfolio.agentguard.domain.model;

import com.portfolio.agentguard.domain.valueobject.RiskDecisionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "risk_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskDecision {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "purchase_intent_id", nullable = false, length = 64)
    private String purchaseIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RiskDecisionType decision;

    @Column(nullable = false)
    private int score;

    @Column(name = "reason_codes", nullable = false, columnDefinition = "TEXT")
    private String reasonCodes; // comma-separated

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "requires_human_approval", nullable = false)
    private boolean requiresHumanApproval;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
