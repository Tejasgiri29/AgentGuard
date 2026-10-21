package com.portfolio.agentguard.api.rest;

import com.portfolio.agentguard.api.dto.request.ResolveApprovalRequest;
import com.portfolio.agentguard.application.service.ApprovalService;
import com.portfolio.agentguard.config.TenantContext;
import com.portfolio.agentguard.domain.model.Approval;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Human-in-the-Loop Approvals", description = "Operator task inbox for reviewing anomalous agent intents")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/pending")
    @Operation(summary = "List pending approval tasks")
    public ResponseEntity<List<Approval>> listPendingApprovals() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(approvalService.listPendingApprovals(tenantId));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Approve or reject a pending purchase intent")
    public ResponseEntity<Approval> resolveApproval(
        @PathVariable String id,
        @Valid @RequestBody ResolveApprovalRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(approvalService.resolveApproval(
            tenantId, id, request.approverUserId(), request.approved(), request.notes()
        ));
    }
}
