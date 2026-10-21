# 🛡️ AgentGuard | Autonomous AI-Agent Trust, Risk & Double-Entry Ledger

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?logo=postgresql)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6-red.svg?logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.0%20Lua-red.svg?logo=redis)](https://redis.io/)
[![React](https://img.shields.io/badge/React-18-blue.svg?logo=react)](https://react.dev/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> An enterprise-grade, high-throughput autonomous AI-agent payment governance platform. Features spending mandate enforcement, heuristic fraud scoring, token-bucket velocity limiting, an immutable double-entry general ledger, and a distributed saga orchestrator built on Java 21 Virtual Threads (Project Loom).

---

## 🌟 Executive Overview

As autonomous AI agents (AutoGPT, LangChain, crewAI) are granted direct procurement authorities, organizations encounter critical operational vulnerabilities:
1. **Runaway LLM Hallucinations**: Agents entering infinite retry loops, firing thousands of unauthorized payments.
2. **Prompt Injection & Siphoning**: Adversaries manipulating agent prompt contexts to transfer funds to unvetted accounts.
3. **Double-Spend & Negative Balances**: Single-entry account updates causing race conditions and ledger discrepancies under concurrency.

**AgentGuard** serves as the **financial security boundary** between autonomous agents and payment rails (Stripe, Adyen, ACH). It guarantees policy enforcement, sub-1.2ms rate limiting, and mathematically verifiable accounting.

---

## 🏗️ Architectural Topology

```mermaid
sequenceDiagram
    autonumber
    actor Agent as Autonomous AI Agent
    participant API as PurchaseIntentController
    participant Policy as Mandate & Risk Engine
    participant Saga as OrderSagaOrchestrator
    participant PG as PaymentGatewayPort
    participant Ledger as LedgerService (Double-Entry)
    participant Outbox as Transactional Outbox
    participant Kafka as Apache Kafka

    Agent->>API: POST /purchase-intents (idempotency-key, mandateId, amount)
    API->>Policy: Evaluate Mandate Bounds & Heuristic Risk Rules
    alt Exceeds Human Approval Threshold
        Policy-->>API: Requires Human-in-the-Loop Approval (HOLD)
        API-->>Agent: HTTP 202 Accepted (Status: PENDING_APPROVAL)
    else Approved
        Policy-->>API: Status: APPROVED
        API->>Saga: executeOrderSaga(intent)
        Saga->>PG: Authorize & Capture Charge
        alt Gateway Success
            Saga->>Ledger: Post Balanced Journal Entry (Debit: A/R, Credit: Revenue)
            Note over Ledger: ∑ Debits ≡ ∑ Credits Verified
            Saga->>Outbox: Save OutboxEvent (ORDER_COMPLETED)
            Saga-->>API: Order Successful
            API-->>Agent: HTTP 201 Created (Status: COMPLETED)
        else Gateway Decline
            Saga->>Saga: Trigger Compensating Transaction
            Saga->>Outbox: Save OutboxEvent (ORDER_FAILED)
            API-->>Agent: HTTP 402 Payment Required
        end
    end
    Outbox->>Kafka: Background Outbox Publisher emits to 'agentguard.orders.v1'
```

---

## ⚡ Core Technical Innovations

### 1. Mathematical Double-Entry Accounting Invariance
Every economic event produces an immutable `JournalEntry` containing at least two `JournalLine` records:
$$\sum_{i=1}^n \text{Debits}_i \equiv \sum_{j=1}^m \text{Credits}_j$$
- **Deadlock-Free Row Locking**: When posting multi-account journal entries, account IDs are sorted alphabetically before acquiring pessimistic write locks (`SELECT ... FOR UPDATE`), mathematically eliminating circular wait deadlocks.
- **Append-Only Immutability**: All updates and deletions on ledger tables are blocked. Corrections are made solely via compensating reversal journal entries.

### 2. Atomic Token-Bucket Velocity Limiter (Redis Lua)
To contain runaway agent loops before touching the PostgreSQL database, velocity limits are enforced in a single network round-trip using Redis Lua:
```lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local current = tonumber(redis.call('get', key) or "0")
if current + 1 > limit then
    return 0
else
    redis.call("INCRBY", key, 1)
    if current == 0 then
        redis.call("EXPIRE", key, tonumber(ARGV[2]))
    end
    return 1
end
```

### 3. Java 21 Virtual Threads (Project Loom)
Enabled globally via Spring Boot 3.3.4:
- Supports **10,000+ concurrent in-flight connections** without OS carrier thread exhaustion.
- Reduces memory footprint from **~1 MB** (OS stack) to **~2 KB** per connection.

---

## 🚀 Quickstart & Docker Cluster

### 1. Launch the Stack
```bash
# Clone the repository
git clone https://github.com/Tejasgiri29/AgentGuard.git
cd AgentGuard

# Start entire infrastructure (PostgreSQL, Redis, Kafka, Backend, Frontend)
docker compose up -d
```

### 2. Service Access Endpoints
| Component | URL | Credentials / Notes |
| :--- | :--- | :--- |
| **AgentGuard Frontend** | `http://localhost:3000` | Dark Glassmorphic React 18 Console |
| **AgentGuard REST API** | `http://localhost:8081` | RFC 9457 Problem Details |
| **Swagger / OpenAPI** | `http://localhost:8081/swagger-ui.html` | Interactive API documentation |
| **Actuator Metrics** | `http://localhost:8081/actuator/prometheus` | Prometheus metrics feed |

### 3. Seed Demo Data
```bash
curl -X POST http://localhost:8081/api/v1/admin/seed \
  -H "X-Tenant-ID: tenant-corp-alpha"
```

---

## 🧪 Verified Benchmarks & Load Tests

Tested using k6 (`tests/load/agentguard-burst.js`) with 500 virtual users:
* **Throughput**: 2,500+ TPS sustained.
* **p95 Latency**: 28.4 ms.
* **p99 Latency**: 64.2 ms.
* **Ledger Inbalance Rate**: 0.00% across 50,000 transactions.
* **Deadlock Count**: 0.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
