export interface DashboardStats {
  totalIntents: number;
  pendingApprovals: number;
  activeMandates: number;
  totalSettledAmount: number;
  paymentSuccessRate: number;
  activeSubscriptions: number;
  ledgerBalanced: boolean;
}

export interface Mandate {
  id: string;
  name: string;
  description: string;
  agentId: string;
  maxSingleTransactionAmount: number;
  maxDailyAmount: number;
  maxMonthlyAmount: number;
  allowedCategories: string;
  allowedMerchants: string;
  blockedMerchants: string;
  requireApprovalAboveAmount: number;
  allowRecurringSubscriptions: boolean;
  status: 'ACTIVE' | 'REVOKED' | 'EXPIRED';
  version: number;
  createdAt: string;
}

export interface PurchaseIntent {
  id: string;
  agentId: string;
  mandateId: string;
  merchantName: string;
  merchantCategory: string;
  amount: number;
  currency: string;
  description: string;
  status: 'CREATED' | 'AWAITING_HUMAN_APPROVAL' | 'APPROVED' | 'COMPLETED' | 'DECLINED' | 'FAILED';
  createdAt: string;
}

export interface Approval {
  id: string;
  purchaseIntentId: string;
  approverUserId?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  notes: string;
  createdAt: string;
}

export interface Payment {
  id: string;
  purchaseIntentId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'CAPTURED' | 'FAILED' | 'REFUNDED';
  provider: string;
  providerTransactionId: string;
  createdAt: string;
}

export interface LedgerAccount {
  id: string;
  accountNumber: string;
  name: string;
  type: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
  balance: number;
  currency: string;
  active: boolean;
}

export interface JournalEntry {
  id: string;
  referenceId: string;
  description: string;
  postedAt: string;
  reversal: boolean;
  lines: Array<{
    id: string;
    accountId: string;
    entryType: 'DEBIT' | 'CREDIT';
    amount: number;
  }>;
}

export interface TrialBalance {
  totalDebits: number;
  totalCredits: number;
  isBalanced: boolean;
}

export interface Subscription {
  id: string;
  agentId: string;
  planId: string;
  status: 'ACTIVE' | 'PAST_DUE' | 'SUSPENDED' | 'CANCELLED';
  currentPeriodStart: string;
  currentPeriodEnd: string;
  dunningStep: number;
}

export interface Invoice {
  id: string;
  subscriptionId: string;
  amount: number;
  currency: string;
  status: 'DRAFT' | 'OPEN' | 'PAID' | 'UNCOLLECTIBLE';
  dueDate: string;
  paidAt?: string;
}

export interface RiskRule {
  id: string;
  name: string;
  ruleType: string;
  weight: number;
  thresholdValue?: number;
  active: boolean;
}

export interface AuditEvent {
  id: string;
  entityName: string;
  entityId: string;
  action: string;
  actorId: string;
  actorType: string;
  payloadSnapshot: string;
  timestamp: string;
}
