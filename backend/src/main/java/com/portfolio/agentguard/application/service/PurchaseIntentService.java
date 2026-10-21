package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.application.port.NotificationPort;
import com.portfolio.agentguard.application.saga.OrderSagaOrchestrator;
import com.portfolio.agentguard.config.KafkaConfig;
import com.portfolio.agentguard.domain.exception.MandateViolationException;
import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.*;
import com.portfolio.agentguard.domain.policy.MandatePolicy;
import com.portfolio.agentguard.domain.policy.RiskPolicyEngine;
import com.portfolio.agentguard.domain.repository.*;
import com.portfolio.agentguard.domain.valueobject.PurchaseIntentStatus;
import com.portfolio.agentguard.domain.valueobject.RiskDecisionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseIntentService {

    private final PurchaseIntentRepository purchaseIntentRepository;
    private final MandateRepository mandateRepository;
    private final RiskDecisionRepository riskDecisionRepository;
    private final ApprovalRepository approvalRepository;
    private final OutboxRepository outboxRepository;
    private final MandatePolicy mandatePolicy;
    private final RiskPolicyEngine riskPolicyEngine;
    private final RateLimiterService rateLimiterService;
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final NotificationPort notificationPort;
    private final AuditService auditService;

    public record CreateIntentCommand(
        String mandateId,
        String agentId,
        String merchantName,
        String merchantCategory,
        BigDecimal amount,
        String currency,
        String description
    ) {}

    @Transactional
    public PurchaseIntent createPurchaseIntent(String tenantId, CreateIntentCommand command) {
        log.info("Processing Purchase Intent for Agent: {} in Tenant: {}", command.agentId(), tenantId);

        // 1. Rate Limiting Check (Token Bucket)
        rateLimiterService.checkRateLimit(tenantId, command.agentId(), 60, 10);

        // 2. Mandate Verification
        Mandate mandate = mandateRepository.findByIdAndTenantId(command.mandateId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Mandate not found: " + command.mandateId()));

        if (!mandate.getAgentId().equals(command.agentId())) {
            throw new MandateViolationException("Agent " + command.agentId() + " is not authorized on this mandate", "AGENT_MISMATCH");
        }

        // 3. Mandate Policy Compliance Checks (Single Limit, Blocked Merchants, etc.)
        mandatePolicy.validateCompliance(mandate, PurchaseIntent.builder()
            .amount(command.amount())
            .merchantName(command.merchantName())
            .merchantCategory(command.merchantCategory())
            .build(), BigDecimal.ZERO);

        // 4. Create and persist PurchaseIntent
        String intentId = "pi-" + UUID.randomUUID();
        PurchaseIntent intent = PurchaseIntent.builder()
            .id(intentId)
            .tenantId(tenantId)
            .agentId(command.agentId())
            .mandateId(mandate.getId())
            .merchantName(command.merchantName())
            .merchantCategory(command.merchantCategory())
            .amount(command.amount())
            .currency(command.currency() != null ? command.currency() : "USD")
            .description(command.description())
            .status(PurchaseIntentStatus.CREATED)
            .build();
        purchaseIntentRepository.save(intent);

        // 5. Evaluate Multi-Vector Risk Engine
        int recentCount = purchaseIntentRepository.countRecentIntentsByAgent(tenantId, command.agentId(), Instant.now().minus(5, ChronoUnit.MINUTES));
        RiskDecision riskDecision = riskPolicyEngine.evaluate(mandate, intent, recentCount);
        riskDecisionRepository.save(riskDecision);

        // 6. Record Outbox Event for Kafka
        OutboxEvent outboxEvent = OutboxEvent.builder()
            .id("out-" + UUID.randomUUID())
            .tenantId(tenantId)
            .topic(KafkaConfig.TOPIC_PURCHASE_INTENT_CREATED)
            .eventKey(intentId)
            .eventType("PurchaseIntentCreatedEvent")
            .payload("{\"id\":\"" + intentId + "\",\"amount\":" + intent.getAmount() + ",\"decision\":\"" + riskDecision.getDecision() + "\"}")
            .status("PENDING")
            .build();
        outboxRepository.save(outboxEvent);

        // 7. Route based on Risk Decision
        if (riskDecision.getDecision() == RiskDecisionType.APPROVE) {
            intent.setStatus(PurchaseIntentStatus.APPROVED);
            purchaseIntentRepository.save(intent);
            // Trigger asynchronous or transactional order fulfillment saga
            sagaOrchestrator.executeOrderFulfillmentSaga(tenantId, intentId);

        } else if (riskDecision.getDecision() == RiskDecisionType.REVIEW) {
            intent.setStatus(PurchaseIntentStatus.AWAITING_HUMAN_APPROVAL);
            purchaseIntentRepository.save(intent);

            Approval approval = Approval.builder()
                .id("appr-" + UUID.randomUUID())
                .tenantId(tenantId)
                .purchaseIntentId(intentId)
                .status("PENDING")
                .notes("Risk Score: " + riskDecision.getScore() + ". " + riskDecision.getExplanation())
                .build();
            approvalRepository.save(approval);

            notificationPort.notifyApprovalRequired(tenantId, intentId, "admin@alphacorp.com", riskDecision.getExplanation());

        } else { // DECLINE
            intent.setStatus(PurchaseIntentStatus.DECLINED);
            purchaseIntentRepository.save(intent);
            notificationPort.notifyPurchaseDeclined(tenantId, intentId, "admin@alphacorp.com", riskDecision.getExplanation());
        }

        auditService.record(tenantId, "PURCHASE_INTENT", intentId, "SUBMITTED", command.agentId(), "AGENT", "Risk Decision: " + riskDecision.getDecision());

        return intent;
    }

    public List<PurchaseIntent> listIntents(String tenantId) {
        return purchaseIntentRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    public PurchaseIntent getIntent(String tenantId, String intentId) {
        return purchaseIntentRepository.findByIdAndTenantId(intentId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase intent not found: " + intentId));
    }
}
