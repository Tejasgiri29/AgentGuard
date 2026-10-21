package com.portfolio.agentguard.domain.policy;

import com.portfolio.agentguard.domain.model.Mandate;
import com.portfolio.agentguard.domain.model.PurchaseIntent;
import com.portfolio.agentguard.domain.model.RiskDecision;
import com.portfolio.agentguard.domain.valueobject.RiskDecisionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RiskPolicyEngine {

    public RiskDecision evaluate(Mandate mandate, PurchaseIntent intent, int recentIntentCount) {
        int riskScore = 0;
        List<String> reasonCodes = new ArrayList<>();
        List<String> explanations = new ArrayList<>();
        boolean requiresHuman = false;

        // Vector 1: Mandate approval threshold
        if (intent.getAmount().compareTo(mandate.getRequireApprovalAboveAmount()) > 0) {
            riskScore += 35;
            reasonCodes.add("AMOUNT_ABOVE_THRESHOLD");
            explanations.add("Amount ($" + intent.getAmount() + ") exceeds automatic threshold ($" + mandate.getRequireApprovalAboveAmount() + ")");
            requiresHuman = true;
        }

        // Vector 2: High Agent Velocity
        if (recentIntentCount >= 4) {
            riskScore += 30;
            reasonCodes.add("AGENT_VELOCITY_HIGH");
            explanations.add("Agent submitted " + recentIntentCount + " purchase intents within the last 5 minutes");
        }

        // Vector 3: Off-Hours Transaction
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(5, 0))) {
            riskScore += 15;
            reasonCodes.add("OFF_HOURS_ACTIVITY");
            explanations.add("Intent initiated during off-hours window (" + now.getHour() + ":00)");
        }

        // Vector 4: Unlisted Merchant Check
        if (mandate.getAllowedMerchants() != null && !mandate.getAllowedMerchants().isBlank()) {
            boolean isKnown = mandate.getAllowedMerchants().toLowerCase().contains(intent.getMerchantName().toLowerCase());
            if (!isKnown) {
                riskScore += 25;
                reasonCodes.add("NEW_UNVERIFIED_MERCHANT");
                explanations.add("Merchant '" + intent.getMerchantName() + "' is not on the mandate pre-approved whitelist");
            }
        }

        // Vector 5: Recurring subscription check
        if ("SAAS_SUBSCRIPTION".equalsIgnoreCase(intent.getMerchantCategory()) && !mandate.isAllowRecurringSubscriptions()) {
            riskScore += 50;
            reasonCodes.add("RECURRING_SUB_UNAUTHORIZED");
            explanations.add("Mandate explicitly disallows recurring subscription commitments");
            requiresHuman = true;
        }

        // Determine final decision
        RiskDecisionType decisionType;
        if (riskScore >= 70) {
            decisionType = RiskDecisionType.DECLINE;
        } else if (riskScore >= 30 || requiresHuman) {
            decisionType = RiskDecisionType.REVIEW;
            requiresHuman = true;
        } else {
            decisionType = RiskDecisionType.APPROVE;
        }

        return RiskDecision.builder()
            .id("risk-" + UUID.randomUUID())
            .tenantId(intent.getTenantId())
            .purchaseIntentId(intent.getId())
            .decision(decisionType)
            .score(riskScore)
            .reasonCodes(String.join(",", reasonCodes))
            .explanation(explanations.isEmpty() ? "Low-risk intent within all mandate boundaries" : String.join("; ", explanations))
            .requiresHumanApproval(requiresHuman)
            .build();
    }
}
