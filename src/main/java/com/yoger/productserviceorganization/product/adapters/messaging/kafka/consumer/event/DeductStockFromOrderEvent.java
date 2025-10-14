package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.data.OrderItemData;
import java.time.LocalDateTime;
import java.util.List;

public record DeductStockFromOrderEvent(
        String orderId,
        String eventId,
        String eventType,
        DeductStockFromOrderData data,
        LocalDateTime occurrenceDateTime
) {
    public record DeductStockFromOrderData(
            Long userId,
            List<OrderItemData> orderItems
    ) {
        public static DeductStockFromOrderData of(
                Long userId,
                List<OrderItemData> orderItems
        ) {
            return new DeductStockFromOrderData(userId, List.copyOf(orderItems));
        }
    }
}
