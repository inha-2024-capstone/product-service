package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeductionFailedEvent(
        String eventId,
        Long productId,
        String eventType,
        DeductionFailedEventData data,
        LocalDateTime occurrenceDateTime,
        String tracingSpanContext
) {
    public record DeductionFailedEventData(
            String orderId,
            Integer orderQuantity
    ) {
        private static DeductionFailedEventData of(String orderId, Integer orderQuantity) {
            return new DeductionFailedEventData(orderId, orderQuantity);
        }
    }

    public static DeductionFailedEvent from(Long productId, DeductStockCommandFromOrder command, String tracingProps) {
        return new DeductionFailedEvent(
                UUID.randomUUID().toString(),
                productId,
                "deductionFailed",
                DeductionFailedEventData.of(
                        command.getOrderId(),
                        command.getDeductStockCommand().getQuantity()
                ),
                LocalDateTime.now(),
                tracingProps
        );
    }
}
