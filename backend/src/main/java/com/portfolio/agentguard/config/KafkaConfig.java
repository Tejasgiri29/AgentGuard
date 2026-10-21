package com.portfolio.agentguard.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_PURCHASE_INTENT_CREATED = "agentguard.purchase-intent-created";
    public static final String TOPIC_RISK_EVALUATED = "agentguard.risk-evaluated";
    public static final String TOPIC_APPROVAL_REQUESTED = "agentguard.approval-requested";
    public static final String TOPIC_ORDER_RESERVED = "agentguard.order-reserved";
    public static final String TOPIC_PAYMENT_CAPTURED = "agentguard.payment-captured";
    public static final String TOPIC_LEDGER_POSTED = "agentguard.ledger-posted";
    public static final String TOPIC_DLQ = "agentguard.dlq";

    @Bean
    public NewTopic purchaseIntentCreatedTopic() {
        return TopicBuilder.name(TOPIC_PURCHASE_INTENT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic riskEvaluatedTopic() {
        return TopicBuilder.name(TOPIC_RISK_EVALUATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(TOPIC_DLQ).partitions(1).replicas(1).build();
    }
}
