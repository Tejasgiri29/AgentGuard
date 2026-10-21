-- AgentGuard V2 Seed Data
-- Fulfills Section 5.14 specification: 2 tenants, 5 users, 4 AI agents, 8 mandates, ledger accounts, etc.

-- Tenants
INSERT INTO tenants (id, name, status) VALUES
('tenant-corp-alpha', 'AlphaCorp Global Innovations', 'ACTIVE'),
('tenant-fintech-beta', 'BetaScale Financial Technologies', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Users
INSERT INTO users (id, tenant_id, email, name, role, status) VALUES
('usr-alpha-admin', 'tenant-corp-alpha', 'admin@agentguard.internal', 'Alice Vance (Admin)', 'ADMIN', 'ACTIVE'),
('usr-alpha-finance', 'tenant-corp-alpha', 'finance@alphacorp.com', 'Bob Smith (Finance Director)', 'OPERATOR', 'ACTIVE'),
('usr-alpha-dev', 'tenant-corp-alpha', 'dev@alphacorp.com', 'Charlie Brown (Tech Lead)', 'USER', 'ACTIVE'),
('usr-beta-admin', 'tenant-fintech-beta', 'admin@betascale.com', 'Diana Prince (Fintech VP)', 'ADMIN', 'ACTIVE'),
('usr-beta-auditor', 'tenant-fintech-beta', 'compliance@betascale.com', 'Edward Norton (Lead Auditor)', 'AUDITOR', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- AI Agents
INSERT INTO agents (id, tenant_id, user_id, name, model_name, purpose, status, api_key_hash) VALUES
('agent-research-01', 'tenant-corp-alpha', 'usr-alpha-dev', 'Autonomous Research Scout', 'gpt-4o-enterprise', 'Procures research papers, datasets, and tool licenses', 'ACTIVE', 'hash_agent_key_001'),
('agent-cloudops-02', 'tenant-corp-alpha', 'usr-alpha-dev', 'Cloud Infrastructure Optimizer', 'claude-3-5-sonnet', 'Auto-reserves compute instances and SaaS observability subscriptions', 'ACTIVE', 'hash_agent_key_002'),
('agent-procure-03', 'tenant-fintech-beta', 'usr-beta-admin', 'Fintech Procurement Agent', 'gemini-1-5-pro', 'Manages API subscriptions, KYC data vendor fees, and sandbox credits', 'ACTIVE', 'hash_agent_key_003'),
('agent-office-04', 'tenant-corp-alpha', 'usr-alpha-finance', 'Office Ops Agent', 'gpt-4o-mini', 'Purchases office equipment and virtual meeting tools', 'ACTIVE', 'hash_agent_key_004')
ON CONFLICT (id) DO NOTHING;

-- Mandates
INSERT INTO mandates (id, tenant_id, user_id, agent_id, name, description, max_single_transaction_amount, max_daily_amount, max_monthly_amount, allowed_categories, blocked_merchants, allowed_merchants, require_approval_above_amount, allow_recurring_subscriptions, valid_from, valid_to, status, version) VALUES
('mnd-research-standard', 'tenant-corp-alpha', 'usr-alpha-dev', 'agent-research-01', 'AI Research Tools Mandate', 'Grants authority for research SaaS and publications', 250.00, 500.00, 3000.00, 'SAAS_SUBSCRIPTION,DATASET,BOOK', 'UntrustedGambling,CryptoShady', 'Notion,OpenAI,GitHub,ArXiv,IEEE', 100.00, TRUE, NOW() - INTERVAL '30 days', NOW() + INTERVAL '335 days', 'ACTIVE', 1),
('mnd-research-micro', 'tenant-corp-alpha', 'usr-alpha-dev', 'agent-research-01', 'Micro-Purchases Fast-Track', 'Small one-off developer utility purchases', 50.00, 100.00, 500.00, 'DEVELOPER_TOOLS', 'CryptoShady', 'AWS,GCP,Heroku,Vercel', 40.00, FALSE, NOW() - INTERVAL '10 days', NOW() + INTERVAL '355 days', 'ACTIVE', 1),
('mnd-cloudops-prod', 'tenant-corp-alpha', 'usr-alpha-dev', 'agent-cloudops-02', 'Cloud Infrastructure Allocation', 'Serverless credits and telemetry pipeline fees', 1000.00, 2500.00, 15000.00, 'CLOUD_INFRASTRUCTURE,TELEMETRY', 'ShadyMining', 'AWS,Cloudflare,Datadog,GrafanaCloud', 500.00, TRUE, NOW() - INTERVAL '40 days', NOW() + INTERVAL '325 days', 'ACTIVE', 1),
('mnd-cloudops-canary', 'tenant-corp-alpha', 'usr-alpha-dev', 'agent-cloudops-02', 'Canary Test Subscriptions', 'Testing sandboxes', 150.00, 300.00, 1200.00, 'CLOUD_INFRASTRUCTURE', 'ShadyMining', 'DigitalOcean,Vercel,Fly.io', 75.00, TRUE, NOW() - INTERVAL '15 days', NOW() + INTERVAL '350 days', 'ACTIVE', 1),
('mnd-fintech-kyc', 'tenant-fintech-beta', 'usr-beta-admin', 'agent-procure-03', 'KYC & Compliance Data Feeds', 'Purchasing sanctioned list scans and ID verification credits', 2000.00, 5000.00, 30000.00, 'FINANCIAL_DATA,COMPLIANCE', 'DarkWebLeaks', 'Trulioo,Plaid,LexisNexis', 1000.00, TRUE, NOW() - INTERVAL '60 days', NOW() + INTERVAL '305 days', 'ACTIVE', 1),
('mnd-fintech-cloud', 'tenant-fintech-beta', 'usr-beta-admin', 'agent-procure-03', 'Beta Fintech Server Credits', 'Cloud compute for fintech backtesting', 1200.00, 2400.00, 12000.00, 'CLOUD_INFRASTRUCTURE', 'CryptoCasino', 'AWS,Stripe,GCP', 600.00, TRUE, NOW() - INTERVAL '20 days', NOW() + INTERVAL '345 days', 'ACTIVE', 1),
('mnd-office-supplies', 'tenant-corp-alpha', 'usr-alpha-finance', 'agent-office-04', 'Corporate Virtual Logistics', 'Virtual conferencing and software tools', 300.00, 600.00, 2500.00, 'OFFICE_SUPPLIES,COMMUNICATION', 'ScamMerchants', 'Zoom,Slack,GoogleWorkspace', 150.00, TRUE, NOW() - INTERVAL '5 days', NOW() + INTERVAL '360 days', 'ACTIVE', 1),
('mnd-office-hardware', 'tenant-corp-alpha', 'usr-alpha-finance', 'agent-office-04', 'Ergonomic & Peripherals Supply', 'Emergency keyboard and peripheral procurements', 500.00, 1000.00, 4000.00, 'HARDWARE', 'ScamMerchants', 'Dell,Apple,AmazonBusiness', 250.00, FALSE, NOW() - INTERVAL '5 days', NOW() + INTERVAL '360 days', 'ACTIVE', 1)
ON CONFLICT (id) DO NOTHING;

-- Risk Rules
INSERT INTO risk_rules (id, tenant_id, name, rule_type, weight, threshold_value, is_active, version) VALUES
('rule-01', 'tenant-corp-alpha', 'Mandate Limit Overflow', 'MANDATE_LIMIT_CHECK', 50, 100.00, TRUE, 1),
('rule-02', 'tenant-corp-alpha', 'Velocity Surge Detector', 'AGENT_VELOCITY', 30, 5.00, TRUE, 1),
('rule-03', 'tenant-corp-alpha', 'Untrusted New Merchant Check', 'NEW_MERCHANT', 25, 0.00, TRUE, 1),
('rule-04', 'tenant-corp-alpha', 'Off-Hours Anomaly', 'OFF_HOURS_PURCHASE', 15, 0.00, TRUE, 1),
('rule-05', 'tenant-fintech-beta', 'High-Value Financial Threshold', 'MANDATE_LIMIT_CHECK', 45, 1000.00, TRUE, 1),
('rule-06', 'tenant-fintech-beta', 'Cross-Border Fraud Vector', 'MERCHANT_CATEGORY_RISK', 35, 0.00, TRUE, 1)
ON CONFLICT (id) DO NOTHING;

-- Ledger Accounts (Standard Double-Entry Chart of Accounts for Alpha & Beta)
INSERT INTO ledger_accounts (id, tenant_id, account_number, name, type, balance, currency, is_active) VALUES
('acc-cash-alpha', 'tenant-corp-alpha', '1000-CASH', 'Corporate Master Cash Vault', 'ASSET', 500000.0000, 'USD', TRUE),
('acc-agent-reserve-alpha', 'tenant-corp-alpha', '1010-RESERVE', 'Agent Guard Escrow Reserve', 'ASSET', 50000.0000, 'USD', TRUE),
('acc-merchant-settle-alpha', 'tenant-corp-alpha', '2000-MERCHANT-SETTLE', 'Merchant Settlement Clearing', 'LIABILITY', 0.0000, 'USD', TRUE),
('acc-platform-fee-alpha', 'tenant-corp-alpha', '4000-PLATFORM-REVENUE', 'Platform Trust & Audit Fees', 'REVENUE', 1420.0000, 'USD', TRUE),
('acc-refund-clear-alpha', 'tenant-corp-alpha', '2010-REFUND-CLEARING', 'Refund In-Flight Clearing', 'LIABILITY', 0.0000, 'USD', TRUE),
('acc-cash-beta', 'tenant-fintech-beta', '1000-CASH', 'BetaScale Operating Cash Account', 'ASSET', 750000.0000, 'USD', TRUE),
('acc-merchant-settle-beta', 'tenant-fintech-beta', '2000-MERCHANT-SETTLE', 'Beta Merchant Settlement Clearing', 'LIABILITY', 0.0000, 'USD', TRUE),
('acc-platform-fee-beta', 'tenant-fintech-beta', '4000-PLATFORM-REVENUE', 'Platform Trust & Audit Fees', 'REVENUE', 2850.0000, 'USD', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Seed Sample Journal Entry with Balanced Lines
INSERT INTO journal_entries (id, tenant_id, reference_id, description, posted_at, is_reversal) VALUES
('je-init-alpha-01', 'tenant-corp-alpha', 'pi-seed-01', 'Payment capture for Notion AI License', NOW() - INTERVAL '2 days', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO journal_lines (id, journal_entry_id, account_id, entry_type, amount) VALUES
('jl-01-debit', 'je-init-alpha-01', 'acc-agent-reserve-alpha', 'DEBIT', 49.0000),
('jl-01-credit-merch', 'je-init-alpha-01', 'acc-merchant-settle-alpha', 'CREDIT', 48.0200),
('jl-01-credit-fee', 'je-init-alpha-01', 'acc-platform-fee-alpha', 'CREDIT', 0.9800)
ON CONFLICT (id) DO NOTHING;

-- Subscription Plans
INSERT INTO subscription_plans (id, tenant_id, name, interval, amount, currency) VALUES
('plan-notion-team', 'tenant-corp-alpha', 'Notion Team Workspace AI', 'MONTHLY', 49.00, 'USD'),
('plan-github-copilot-ent', 'tenant-corp-alpha', 'GitHub Enterprise Seat License', 'MONTHLY', 39.00, 'USD'),
('plan-datadog-pro', 'tenant-corp-alpha', 'Datadog APM & Logs Ingestion Tier', 'MONTHLY', 299.00, 'USD'),
('plan-trulioo-kyc', 'tenant-fintech-beta', 'Trulioo Real-Time Identity Feed', 'MONTHLY', 850.00, 'USD')
ON CONFLICT (id) DO NOTHING;

-- Seed Purchase Intents
INSERT INTO purchase_intents (id, tenant_id, agent_id, mandate_id, merchant_name, merchant_category, amount, currency, description, status, created_at) VALUES
('pi-seed-01', 'tenant-corp-alpha', 'agent-research-01', 'mnd-research-standard', 'Notion', 'SAAS_SUBSCRIPTION', 49.00, 'USD', 'Monthly AI workspace subscription', 'COMPLETED', NOW() - INTERVAL '2 days'),
('pi-seed-02', 'tenant-corp-alpha', 'agent-research-01', 'mnd-research-standard', 'IEEE Xplore', 'DATASET', 120.00, 'USD', 'Conference Paper Download', 'APPROVED', NOW() - INTERVAL '1 day'),
('pi-seed-03', 'tenant-corp-alpha', 'agent-research-01', 'mnd-research-standard', 'ArXiv Sponsored Lab', 'DATASET', 350.00, 'USD', 'Preprint dataset archive access', 'AWAITING_HUMAN_APPROVAL', NOW() - INTERVAL '3 hours'),
('pi-seed-04', 'tenant-corp-alpha', 'agent-cloudops-02', 'mnd-cloudops-prod', 'Datadog', 'TELEMETRY', 299.00, 'USD', 'Observability cluster monitoring upgrade', 'COMPLETED', NOW() - INTERVAL '5 days'),
('pi-seed-05', 'tenant-corp-alpha', 'agent-cloudops-02', 'mnd-cloudops-prod', 'AWS Marketplace', 'CLOUD_INFRASTRUCTURE', 850.00, 'USD', 'Elasticache Redis Cluster Reserved Node', 'AWAITING_HUMAN_APPROVAL', NOW() - INTERVAL '1 hour'),
('pi-seed-06', 'tenant-fintech-beta', 'agent-procure-03', 'mnd-fintech-kyc', 'Trulioo', 'COMPLIANCE', 850.00, 'USD', 'Monthly KYC API Verification credits', 'COMPLETED', NOW() - INTERVAL '10 days'),
('pi-seed-07', 'tenant-fintech-beta', 'agent-procure-03', 'mnd-fintech-kyc', 'DarkWebScanUntrusted', 'SECURITY', 2500.00, 'USD', 'Threat feed from unverified origin', 'DECLINED', NOW() - INTERVAL '6 hours')
ON CONFLICT (id) DO NOTHING;

-- Seed Approvals
INSERT INTO approvals (id, tenant_id, purchase_intent_id, approver_user_id, status, notes, created_at) VALUES
('appr-01', 'tenant-corp-alpha', 'pi-seed-03', NULL, 'PENDING', 'Amount ($350) exceeds mandate automatic approval limit of $100.', NOW() - INTERVAL '3 hours'),
('appr-02', 'tenant-corp-alpha', 'pi-seed-05', NULL, 'PENDING', 'Amount ($850) exceeds mandate automatic approval limit of $500.', NOW() - INTERVAL '1 hour')
ON CONFLICT (id) DO NOTHING;

-- Seed Payments
INSERT INTO payments (id, tenant_id, purchase_intent_id, order_id, amount, currency, status, provider, provider_transaction_id, created_at) VALUES
('pay-01', 'tenant-corp-alpha', 'pi-seed-01', NULL, 49.00, 'USD', 'CAPTURED', 'MOCK_STRIPE', 'ch_mock_stripe_9921', NOW() - INTERVAL '2 days'),
('pay-02', 'tenant-corp-alpha', 'pi-seed-04', NULL, 299.00, 'USD', 'CAPTURED', 'MOCK_STRIPE', 'ch_mock_stripe_8812', NOW() - INTERVAL '5 days'),
('pay-03', 'tenant-fintech-beta', 'pi-seed-06', NULL, 850.00, 'USD', 'CAPTURED', 'MOCK_RAZORPAY', 'pay_mock_rzp_4419', NOW() - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

-- Audit Events
INSERT INTO audit_events (id, tenant_id, entity_name, entity_id, action, actor_id, actor_type, payload_snapshot, timestamp) VALUES
('aud-01', 'tenant-corp-alpha', 'MANDATE', 'mnd-research-standard', 'CREATED', 'usr-alpha-dev', 'USER', '{"name":"AI Research Tools Mandate","limit":250.00}', NOW() - INTERVAL '30 days'),
('aud-02', 'tenant-corp-alpha', 'PURCHASE_INTENT', 'pi-seed-01', 'SUBMITTED', 'agent-research-01', 'AGENT', '{"amount":49.00,"merchant":"Notion"}', NOW() - INTERVAL '2 days'),
('aud-03', 'tenant-corp-alpha', 'RISK_DECISION', 'pi-seed-01', 'EVALUATED', 'SYSTEM', '{"decision":"APPROVE","score":12}', NOW() - INTERVAL '2 days'),
('aud-04', 'tenant-corp-alpha', 'PAYMENT', 'pay-01', 'CAPTURED', 'SYSTEM', '{"amount":49.00,"provider":"MOCK_STRIPE"}', NOW() - INTERVAL '2 days'),
('aud-05', 'tenant-corp-alpha', 'PURCHASE_INTENT', 'pi-seed-03', 'ESCALATED_HUMAN_APPROVAL', 'SYSTEM', '{"reason":"Limit breached"}', NOW() - INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;
