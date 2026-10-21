package com.portfolio.agentguard.domain.policy;

import com.portfolio.agentguard.domain.exception.MandateViolationException;
import com.portfolio.agentguard.domain.model.Mandate;
import com.portfolio.agentguard.domain.model.PurchaseIntent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class MandatePolicy {

    public void validateCompliance(Mandate mandate, PurchaseIntent intent, BigDecimal dailySpentSoFar) {
        if (!mandate.isActive()) {
            throw new MandateViolationException("Mandate is inactive, revoked, or expired", "MANDATE_INACTIVE");
        }

        // 1. Single transaction limit
        if (intent.getAmount().compareTo(mandate.getMaxSingleTransactionAmount()) > 0) {
            throw new MandateViolationException(
                "Transaction amount $" + intent.getAmount() + " exceeds maximum single limit of $" + mandate.getMaxSingleTransactionAmount(),
                "SINGLE_LIMIT_EXCEEDED"
            );
        }

        // 2. Daily velocity limit
        BigDecimal projectedDaily = dailySpentSoFar.add(intent.getAmount());
        if (projectedDaily.compareTo(mandate.getMaxDailyAmount()) > 0) {
            throw new MandateViolationException(
                "Transaction would cause daily spend of $" + projectedDaily + " to exceed daily limit of $" + mandate.getMaxDailyAmount(),
                "DAILY_LIMIT_EXCEEDED"
            );
        }

        // 3. Blocked merchants blacklist
        if (mandate.getBlockedMerchants() != null && !mandate.getBlockedMerchants().isBlank()) {
            List<String> blocked = Arrays.stream(mandate.getBlockedMerchants().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
            if (blocked.contains(intent.getMerchantName().toLowerCase())) {
                throw new MandateViolationException("Merchant '" + intent.getMerchantName() + "' is explicitly blocked", "MERCHANT_BLOCKED");
            }
        }

        // 4. Allowed categories check
        if (mandate.getAllowedCategories() != null && !mandate.getAllowedCategories().isBlank()) {
            List<String> allowedCats = Arrays.stream(mandate.getAllowedCategories().split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
            if (!allowedCats.contains(intent.getMerchantCategory().toUpperCase())) {
                throw new MandateViolationException("Category '" + intent.getMerchantCategory() + "' is not permitted in mandate", "CATEGORY_UNAUTHORIZED");
            }
        }
    }

    public boolean requiresHumanApproval(Mandate mandate, PurchaseIntent intent) {
        return intent.getAmount().compareTo(mandate.getRequireApprovalAboveAmount()) >= 0;
    }
}
