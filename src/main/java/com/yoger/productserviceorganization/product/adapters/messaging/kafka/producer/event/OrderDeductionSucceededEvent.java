package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDeductionSucceededEvent(
        String eventId,
        String orderId,
        EventType eventType,
        Data data,
        LocalDateTime occurrenceDateTime,
        String tracingSpanContext
) {
    public static OrderDeductionSucceededEvent from(
            DeductStockFromOrderCommand cmd,
            String tracingSpanContext
    ) {
        return new OrderDeductionSucceededEvent(
                UUID.randomUUID().toString(),
                cmd.getOrderId(),
                EventType.DEDUCTION_COMPLETED,
                Data.from(cmd),
                cmd.getOccurrenceDateTime(),
                tracingSpanContext
        );
    }

    public record Data(
            Long userId,
            List<Item> items
    ) {
        static Data from(DeductStockFromOrderCommand cmd) {
            return new Data(
                    cmd.getUserId(),
                    cmd.getItems().stream()
                            .map(i -> new Item(i.getProductId(), i.getQuantity()))
                            .toList()
            );
        }
    }

    public record Item(Long productId, Integer quantity) {}
}
