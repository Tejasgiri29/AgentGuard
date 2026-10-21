package com.portfolio.agentguard.application.port;

public interface NotificationPort {
    void notifyApprovalRequired(String tenantId, String purchaseIntentId, String approverEmail, String summary);
    void notifyPurchaseCompleted(String tenantId, String purchaseIntentId, String userEmail, String summary);
    void notifyPurchaseDeclined(String tenantId, String purchaseIntentId, String userEmail, String reason);
}
