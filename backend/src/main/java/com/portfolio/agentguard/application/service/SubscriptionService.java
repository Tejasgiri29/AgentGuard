package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.Invoice;
import com.portfolio.agentguard.domain.model.Subscription;
import com.portfolio.agentguard.domain.repository.InvoiceRepository;
import com.portfolio.agentguard.domain.repository.SubscriptionRepository;
import com.portfolio.agentguard.domain.valueobject.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    public List<Subscription> listSubscriptions(String tenantId) {
        return subscriptionRepository.findByTenantId(tenantId);
    }

    public List<Invoice> listInvoices(String tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional
    public Subscription cancelSubscription(String tenantId, String subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndTenantId(subscriptionId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        Subscription saved = subscriptionRepository.save(subscription);
        auditService.record(tenantId, "SUBSCRIPTION", subscriptionId, "CANCELLED", "USER", "USER", "User cancelled subscription");
        return saved;
    }

    @Transactional
    public Invoice generateInvoice(String tenantId, String subscriptionId, BigDecimal amount) {
        Invoice invoice = Invoice.builder()
            .id("inv-" + UUID.randomUUID())
            .tenantId(tenantId)
            .subscriptionId(subscriptionId)
            .amount(amount)
            .currency("USD")
            .status("OPEN")
            .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
            .build();
        return invoiceRepository.save(invoice);
    }

    // Automated Dunning Pipeline (e.g. executed periodically)
    @Scheduled(cron = "0 0 2 * * *") // Run daily at 2 AM
    @Transactional
    public void executeDunningCycle() {
        log.info("Executing automated subscription dunning verification cycle...");
        List<Subscription> pastDueSubs = subscriptionRepository.findByTenantIdAndStatus("tenant-corp-alpha", SubscriptionStatus.PAST_DUE);
        for (Subscription sub : pastDueSubs) {
            int step = sub.getDunningStep() + 1;
            sub.setDunningStep(step);
            if (step >= 4) {
                sub.setStatus(SubscriptionStatus.SUSPENDED);
                log.warn("Subscription {} reached max dunning step. Auto-suspending.", sub.getId());
            }
            subscriptionRepository.save(sub);
        }
    }
}
