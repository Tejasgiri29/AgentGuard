package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.application.saga.OrderSagaOrchestrator;
import com.portfolio.agentguard.domain.exception.ResourceNotFoundException;
import com.portfolio.agentguard.domain.model.Approval;
import com.portfolio.agentguard.domain.model.PurchaseIntent;
import com.portfolio.agentguard.domain.repository.ApprovalRepository;
import com.portfolio.agentguard.domain.repository.PurchaseIntentRepository;
import com.portfolio.agentguard.domain.valueobject.PurchaseIntentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final PurchaseIntentRepository purchaseIntentRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final AuditService auditService;

    public List<Approval> listPendingApprovals(String tenantId) {
        return approvalRepository.findByTenantIdAndStatus(tenantId, "PENDING");
    }

    @Transactional
    public Approval resolveApproval(String tenantId, String approvalId, String approverUserId, boolean approved, String notes) {
        Approval approval = approvalRepository.findByIdAndTenantId(approvalId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Approval task not found: " + approvalId));

        if (!approval.isPending()) {
            throw new IllegalStateException("Approval task has already been resolved with status: " + approval.getStatus());
        }

        approval.setStatus(approved ? "APPROVED" : "REJECTED");
        approval.setApproverUserId(approverUserId);
        approval.setNotes(notes);
        approval.setDecidedAt(Instant.now());
        approvalRepository.save(approval);

        PurchaseIntent intent = purchaseIntentRepository.findByIdAndTenantId(approval.getPurchaseIntentId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase intent not found: " + approval.getPurchaseIntentId()));

        if (approved) {
            log.info("Human approver {} approved Intent {}. Resuming saga...", approverUserId, intent.getId());
            intent.setStatus(PurchaseIntentStatus.APPROVED);
            purchaseIntentRepository.save(intent);
            auditService.record(tenantId, "APPROVAL", approvalId, "APPROVED", approverUserId, "USER", notes);
            // Trigger fulfillment saga
            sagaOrchestrator.executeOrderFulfillmentSaga(tenantId, intent.getId());
        } else {
            log.info("Human approver {} rejected Intent {}", approverUserId, intent.getId());
            intent.setStatus(PurchaseIntentStatus.DECLINED);
            purchaseIntentRepository.save(intent);
            auditService.record(tenantId, "APPROVAL", approvalId, "REJECTED", approverUserId, "USER", notes);
        }

        return approval;
    }
}
