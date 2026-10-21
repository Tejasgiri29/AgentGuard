package com.portfolio.agentguard.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResolveApprovalRequest(
    @NotBlank String approverUserId,
    boolean approved,
    String notes
) {}
