package com.yoger.productserviceorganization.review.adapter.messaging.kafka.consumer.event;

import java.time.LocalDateTime;

public record OrderCompletedEvent(
        String eventId,
        Long orderId,
        String eventType,
        OrderCompletedEventData data,
        LocalDateTime occurrenceDateTime
) {
    public record OrderCompletedEventData(
            Long userId,
            Long productId,
            Integer orderQuantity
    ) {
    }
}
