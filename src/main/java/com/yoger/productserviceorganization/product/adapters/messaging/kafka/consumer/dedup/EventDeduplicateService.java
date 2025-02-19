package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.dedup;

public interface EventDeduplicateService {
    boolean isDuplicate(String eventId);

    void putKey(String eventId);
}
