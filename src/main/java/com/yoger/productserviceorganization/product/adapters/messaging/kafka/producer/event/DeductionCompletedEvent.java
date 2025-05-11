package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeductionCompletedEvent(
        String eventId,
        Long productId,
        String eventType,
        DeductionCompletedEventData data,
        LocalDateTime occurrenceDateTime

) {
    public record DeductionCompletedEventData(
            Long orderId,
            Integer orderQuantity
    ) {
        private static DeductionCompletedEventData of(Long orderId, Integer orderQuantity) {
            return new DeductionCompletedEventData(orderId, orderQuantity);
        }
    }

    public static DeductionCompletedEvent from(Long productId, DeductStockCommandFromOrder command) {
        return new DeductionCompletedEvent(
                UUID.randomUUID().toString(),
                productId,
                "deductionCompleted",
                DeductionCompletedEventData.of(
                        command.getOrderId(),
                        command.getDeductStockCommand().getQuantity()
                ),
                LocalDateTime.now()
        );
    }
}
