package com.portfolio.agentguard.application.saga;

import com.portfolio.agentguard.application.port.NotificationPort;
import com.portfolio.agentguard.application.port.PaymentGatewayPort;
import com.portfolio.agentguard.application.service.AuditService;
import com.portfolio.agentguard.application.service.LedgerService;
import com.portfolio.agentguard.domain.model.*;
import com.portfolio.agentguard.domain.repository.*;
import com.portfolio.agentguard.domain.valueobject.EntryType;
import com.portfolio.agentguard.domain.valueobject.PaymentStatus;
import com.portfolio.agentguard.domain.valueobject.PurchaseIntentStatus;
import com.portfolio.agentguard.domain.valueobject.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final PurchaseIntentRepository purchaseIntentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SagaExecutionRepository sagaExecutionRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerService ledgerService;
    private final PaymentGatewayPort paymentGatewayPort;
    private final NotificationPort notificationPort;
    private final AuditService auditService;

    @Transactional
    public void executeOrderFulfillmentSaga(String tenantId, String purchaseIntentId) {
        log.info("Starting Order Fulfillment Saga for Intent: {} (Tenant: {})", purchaseIntentId, tenantId);

        PurchaseIntent intent = purchaseIntentRepository.findByIdAndTenantId(purchaseIntentId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase intent not found: " + purchaseIntentId));

        String sagaId = "saga-" + UUID.randomUUID();
        SagaExecution saga = SagaExecution.builder()
            .sagaId(sagaId)
            .tenantId(tenantId)
            .sagaType("PURCHASE_FULFILLMENT")
            .status(SagaStatus.IN_PROGRESS)
            .currentStep("RESERVING_ORDER")
            .payload("{\"purchaseIntentId\":\"" + purchaseIntentId + "\",\"amount\":" + intent.getAmount() + "}")
            .retryCount(0)
            .build();
        sagaExecutionRepository.save(saga);

        try {
            // STEP 1: Reserve Order
            Order order = Order.builder()
                .id("ord-" + UUID.randomUUID())
                .tenantId(tenantId)
                .purchaseIntentId(intent.getId())
                .merchantOrderRef("REF-" + System.currentTimeMillis())
                .amount(intent.getAmount())
                .currency(intent.getCurrency())
                .status("RESERVED")
                .build();
            orderRepository.save(order);
            saga.setCurrentStep("AUTHORIZING_PAYMENT");
            sagaExecutionRepository.save(saga);

            // STEP 2: Process Payment via Payment Gateway Port
            PaymentGatewayPort.PaymentResult payResult = paymentGatewayPort.authorizeAndCapture(
                tenantId, intent.getId(), intent.getAmount(), intent.getCurrency(), intent.getMerchantName()
            );

            if (!payResult.success()) {
                // Trigger Compensating Transaction
                log.warn("Payment failed for Intent {}: {}. Triggering compensation...", intent.getId(), payResult.errorCode());
                compensate(tenantId, saga, order, intent, "Payment declined: " + payResult.errorCode());
                return;
            }

            // Record successful payment
            Payment payment = Payment.builder()
                .id("pay-" + UUID.randomUUID())
                .tenantId(tenantId)
                .purchaseIntentId(intent.getId())
                .orderId(order.getId())
                .amount(intent.getAmount())
                .currency(intent.getCurrency())
                .status(PaymentStatus.CAPTURED)
                .provider("MOCK_GATEWAY")
                .providerTransactionId(payResult.transactionId())
                .build();
            paymentRepository.save(payment);
            saga.setCurrentStep("POSTING_LEDGER");
            sagaExecutionRepository.save(saga);

            // STEP 3: Post Balanced Double-Entry Ledger Entries
            postLedgerTransfer(tenantId, intent, payment);

            // STEP 4: Complete Saga & Notify
            intent.setStatus(PurchaseIntentStatus.COMPLETED);
            purchaseIntentRepository.save(intent);

            order.setStatus("COMPLETED");
            orderRepository.save(order);

            saga.setStatus(SagaStatus.COMPLETED);
            saga.setCurrentStep("COMPLETED");
            sagaExecutionRepository.save(saga);

            notificationPort.notifyPurchaseCompleted(
                tenantId, intent.getId(), "approver@internal",
                "Purchase of $" + intent.getAmount() + " at " + intent.getMerchantName() + " successfully settled."
            );
            auditService.record(tenantId, "ORDER_SAGA", sagaId, "COMPLETED", intent.getAgentId(), "AGENT", "Order fulfilled");

            log.info("Order Fulfillment Saga completed successfully for Intent: {}", purchaseIntentId);

        } catch (Exception ex) {
            log.error("Unhandled error in order saga, initiating compensation: {}", ex.getMessage(), ex);
            compensate(tenantId, saga, null, intent, ex.getMessage());
        }
    }

    private void postLedgerTransfer(String tenantId, PurchaseIntent intent, Payment payment) {
        // Retrieve standard accounts or fallback
        List<LedgerAccount> accounts = ledgerAccountRepository.findByTenantId(tenantId);
        if (accounts.size() >= 3) {
            String reserveAccId = accounts.get(1).getId();
            String merchantSettleAccId = accounts.get(2).getId();
            String feeAccId = accounts.size() > 3 ? accounts.get(3).getId() : merchantSettleAccId;

            BigDecimal platformFee = intent.getAmount().multiply(new BigDecimal("0.02")).setScale(4, java.math.RoundingMode.HALF_UP);
            BigDecimal netMerchant = intent.getAmount().subtract(platformFee);

            List<LedgerService.LineItem> items = List.of(
                new LedgerService.LineItem(reserveAccId, EntryType.DEBIT, intent.getAmount()),
                new LedgerService.LineItem(merchantSettleAccId, EntryType.CREDIT, netMerchant),
                new LedgerService.LineItem(feeAccId, EntryType.CREDIT, platformFee)
            );
            ledgerService.postJournalEntry(tenantId, payment.getId(), "Payment settlement for " + intent.getMerchantName(), items);
        }
    }

    private void compensate(String tenantId, SagaExecution saga, Order order, PurchaseIntent intent, String reason) {
        log.info("Executing compensating transaction for saga: {}", saga.getSagaId());
        saga.setStatus(SagaStatus.COMPENSATING);
        sagaExecutionRepository.save(saga);

        if (order != null) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }

        intent.setStatus(PurchaseIntentStatus.FAILED);
        purchaseIntentRepository.save(intent);

        saga.setStatus(SagaStatus.FAILED);
        saga.setCurrentStep("FAILED: " + reason);
        sagaExecutionRepository.save(saga);

        notificationPort.notifyPurchaseDeclined(tenantId, intent.getId(), "approver@internal", reason);
        auditService.record(tenantId, "ORDER_SAGA", saga.getSagaId(), "COMPENSATED_FAILED", intent.getAgentId(), "AGENT", reason);
    }
}
