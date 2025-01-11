package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event;

import java.time.LocalDateTime;

public record OrderCanceledEvent(
        String eventId,
        Long orderId,
        String eventType,
        OrderCanceledEventData data,
        LocalDateTime occurrenceDateTime
) {
    public record OrderCanceledEventData(
            Long userId,
            Long productId,
            Integer orderQuantity
    ) {
    }
}
