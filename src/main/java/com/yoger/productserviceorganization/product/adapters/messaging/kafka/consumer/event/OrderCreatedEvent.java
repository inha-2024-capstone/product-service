package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        String eventId,
        Long orderId,
        String eventType,
        OrderCreatedEventData data,
        LocalDateTime occurrenceDateTime
) {
    public record OrderCreatedEventData(
            Long userId,
            Long productId,
            Integer orderQuantity
    ) {
    }
}
