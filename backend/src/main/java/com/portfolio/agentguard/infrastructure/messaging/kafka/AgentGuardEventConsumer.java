package com.portfolio.agentguard.infrastructure.messaging.kafka;

import com.portfolio.agentguard.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentGuardEventConsumer {

    @KafkaListener(topics = KafkaConfig.TOPIC_PURCHASE_INTENT_CREATED, groupId = "agentguard-analytics-consumer")
    public void onPurchaseIntentCreated(
        @Payload String payload,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("[Kafka Consumer] Received Intent Event. Key: {}, Partition: {}, Offset: {}, Payload: {}",
            key, partition, offset, payload);
    }
}
