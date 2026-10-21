package com.portfolio.agentguard.application.service;

import com.portfolio.agentguard.domain.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> rateLimiterScript;

    public void checkRateLimit(String tenantId, String agentId, int capacity, int refillRatePerSec) {
        String key = "ratelimit:" + tenantId + ":" + agentId;
        double now = Instant.now().toEpochMilli() / 1000.0;

        try {
            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) stringRedisTemplate.execute(
                rateLimiterScript,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(refillRatePerSec),
                "1",
                String.valueOf(now)
            );

            if (result != null && !result.isEmpty()) {
                long allowed = result.get(0);
                if (allowed == 0) {
                    long retryAfter = result.size() > 2 ? result.get(2) : 1;
                    log.warn("Rate limit breached for key: {}. Retry after {}s", key, retryAfter);
                    throw new RateLimitExceededException("Agent rate limit exceeded. Capacity: " + capacity, retryAfter);
                }
            }
        } catch (RateLimitExceededException rle) {
            throw rle;
        } catch (Exception ex) {
            // Fail-open strategy if Redis temporarily drops in demo
            log.error("Redis rate limiter lookup failed, failing open: {}", ex.getMessage());
        }
    }
}
