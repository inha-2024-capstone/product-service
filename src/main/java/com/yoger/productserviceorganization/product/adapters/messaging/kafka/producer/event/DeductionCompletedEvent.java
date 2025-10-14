package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeductionCompletedEvent(
        String eventId,
        Long productId,
        String eventType,
        DeductionCompletedEventData data,
        LocalDateTime occurrenceDateTime,
        String tracingSpanContext
) {
    public record DeductionCompletedEventData(
            String orderId,
            Integer orderQuantity
    ) {
        private static DeductionCompletedEventData of(String orderId, Integer orderQuantity) {
            return new DeductionCompletedEventData(orderId, orderQuantity);
        }
    }

    public static DeductionCompletedEvent from(Long productId, DeductStockCommandFromOrder command, String tracingSpanContext) {
        return new DeductionCompletedEvent(
                UUID.randomUUID().toString(),
                productId,
                "deductionCompleted",
                DeductionCompletedEventData.of(
                        command.getOrderId(),
                        command.getDeductStockCommand().getQuantity()
                ),
                LocalDateTime.now(),
                tracingSpanContext
        );
    }
}
