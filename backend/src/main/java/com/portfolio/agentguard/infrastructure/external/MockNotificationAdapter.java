package com.portfolio.agentguard.infrastructure.external;

import com.portfolio.agentguard.application.port.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockNotificationAdapter implements NotificationPort {

    @Override
    public void notifyApprovalRequired(String tenantId, String purchaseIntentId, String approverEmail, String summary) {
        log.info("[NOTIFICATION][EMAIL] To: {} | Subject: Action Required: Purchase Approval Pending | Intent: {} | Summary: {}",
            approverEmail, purchaseIntentId, summary);
    }

    @Override
    public void notifyPurchaseCompleted(String tenantId, String purchaseIntentId, String userEmail, String summary) {
        log.info("[NOTIFICATION][SLACK/EMAIL] To: {} | Subject: Agent Purchase Settled | Intent: {} | Summary: {}",
            userEmail, purchaseIntentId, summary);
    }

    @Override
    public void notifyPurchaseDeclined(String tenantId, String purchaseIntentId, String userEmail, String reason) {
        log.info("[NOTIFICATION][ALERT] To: {} | Subject: Agent Purchase Declined | Intent: {} | Reason: {}",
            userEmail, purchaseIntentId, reason);
    }
}
