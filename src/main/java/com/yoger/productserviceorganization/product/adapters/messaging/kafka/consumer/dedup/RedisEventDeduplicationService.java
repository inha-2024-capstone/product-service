package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.dedup;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisEventDeduplicationService implements EventDeduplicateService{
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "processed_event : ";
    private static final Duration EXPIRATION = Duration.ofMinutes(1);

    @Override
    public boolean isDuplicate(String eventId) {
        Boolean exists = redisTemplate.hasKey(KEY_PREFIX + eventId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void putKey(String eventId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + eventId, "0", EXPIRATION);
    }
}
