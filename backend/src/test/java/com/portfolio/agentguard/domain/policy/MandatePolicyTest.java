package com.portfolio.agentguard.domain.policy;

import com.portfolio.agentguard.domain.exception.MandateViolationException;
import com.portfolio.agentguard.domain.model.Mandate;
import com.portfolio.agentguard.domain.model.PurchaseIntent;
import com.portfolio.agentguard.domain.valueobject.MandateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class MandatePolicyTest {

    private MandatePolicy policy;
    private Mandate activeMandate;

    @BeforeEach
    void setUp() {
        policy = new MandatePolicy();
        activeMandate = Mandate.builder()
            .id("mnd-test-1")
            .tenantId("tenant-corp-alpha")
            .agentId("agent-01")
            .name("Test Mandate")
            .maxSingleTransactionAmount(new BigDecimal("100.00"))
            .maxDailyAmount(new BigDecimal("500.00"))
            .maxMonthlyAmount(new BigDecimal("2000.00"))
            .allowedCategories("SAAS_SUBSCRIPTION,DEVELOPER_TOOLS")
            .blockedMerchants("CryptoCasino,DarkWeb")
            .allowedMerchants("GitHub,Notion,AWS")
            .requireApprovalAboveAmount(new BigDecimal("50.00"))
            .status(MandateStatus.ACTIVE)
            .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
            .validTo(Instant.now().plus(30, ChronoUnit.DAYS))
            .build();
    }

    @Test
    @DisplayName("Should pass compliance for intent within limits")
    void testComplianceSuccess() {
        PurchaseIntent intent = PurchaseIntent.builder()
            .amount(new BigDecimal("45.00"))
            .merchantName("Notion")
            .merchantCategory("SAAS_SUBSCRIPTION")
            .build();

        assertDoesNotThrow(() -> policy.validateCompliance(activeMandate, intent, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should throw MandateViolationException when amount exceeds single transaction limit")
    void testSingleLimitExceeded() {
        PurchaseIntent intent = PurchaseIntent.builder()
            .amount(new BigDecimal("150.00"))
            .merchantName("Notion")
            .merchantCategory("SAAS_SUBSCRIPTION")
            .build();

        MandateViolationException ex = assertThrows(
            MandateViolationException.class,
            () -> policy.validateCompliance(activeMandate, intent, BigDecimal.ZERO)
        );
        assertEquals("SINGLE_LIMIT_EXCEEDED", ex.getReasonCode());
    }

    @Test
    @DisplayName("Should throw MandateViolationException when merchant is in blocklist")
    void testBlockedMerchant() {
        PurchaseIntent intent = PurchaseIntent.builder()
            .amount(new BigDecimal("25.00"))
            .merchantName("CryptoCasino")
            .merchantCategory("DEVELOPER_TOOLS")
            .build();

        MandateViolationException ex = assertThrows(
            MandateViolationException.class,
            () -> policy.validateCompliance(activeMandate, intent, BigDecimal.ZERO)
        );
        assertEquals("MERCHANT_BLOCKED", ex.getReasonCode());
    }

    @Test
    @DisplayName("Should throw MandateViolationException when category is unauthorized")
    void testUnauthorizedCategory() {
        PurchaseIntent intent = PurchaseIntent.builder()
            .amount(new BigDecimal("25.00"))
            .merchantName("Airline")
            .merchantCategory("TRAVEL_FLIGHT")
            .build();

        MandateViolationException ex = assertThrows(
            MandateViolationException.class,
            () -> policy.validateCompliance(activeMandate, intent, BigDecimal.ZERO)
        );
        assertEquals("CATEGORY_UNAUTHORIZED", ex.getReasonCode());
    }
}
