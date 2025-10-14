package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event;

import java.time.LocalDateTime;

public record OrderCanceledEvent(
        String eventId,
        String orderId,
        String eventType,
        OrderCanceledData data,
        LocalDateTime occurrenceDateTime
) {
    public record OrderCanceledData(
            Long userId,
            Long productId,
            Integer orderQuantity,
            Boolean isStockOccupied,
            Boolean isPaymentCompleted
    ) {
    }
}
