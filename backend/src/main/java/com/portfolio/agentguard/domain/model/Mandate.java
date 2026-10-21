package com.portfolio.agentguard.domain.model;

import com.portfolio.agentguard.domain.valueobject.MandateStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "mandates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mandate {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "agent_id", nullable = false, length = 64)
    private String agentId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_single_transaction_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal maxSingleTransactionAmount;

    @Column(name = "max_daily_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal maxDailyAmount;

    @Column(name = "max_monthly_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal maxMonthlyAmount;

    @Column(name = "allowed_categories", columnDefinition = "TEXT")
    private String allowedCategories;

    @Column(name = "blocked_merchants", columnDefinition = "TEXT")
    private String blockedMerchants;

    @Column(name = "allowed_merchants", columnDefinition = "TEXT")
    private String allowedMerchants;

    @Column(name = "require_approval_above_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal requireApprovalAboveAmount;

    @Column(name = "allow_recurring_subscriptions", nullable = false)
    private boolean allowRecurringSubscriptions;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MandateStatus status;

    @Version
    @Column(nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (status == null) status = MandateStatus.ACTIVE;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(validTo);
    }

    public boolean isActive() {
        return status == MandateStatus.ACTIVE && !isExpired();
    }
}
