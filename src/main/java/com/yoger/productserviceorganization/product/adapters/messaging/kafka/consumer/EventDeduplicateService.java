package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

interface EventDeduplicateService {
    boolean isDuplicate(String eventId);

    void putKey(String eventId);
}
