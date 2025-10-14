package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        String eventId,
        String orderId,
        String eventType,
        OrderCreatedData data,
        LocalDateTime occurrenceDateTime
) {
    public record OrderCreatedData(
            Long userId,
            Long productId,
            Integer orderQuantity
    ) {
    }
}
