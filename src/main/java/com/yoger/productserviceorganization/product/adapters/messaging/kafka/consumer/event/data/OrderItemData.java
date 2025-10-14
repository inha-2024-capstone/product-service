package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.data;

public record OrderItemData(
        Long productId,
        Integer quantity
) {
    public static OrderItemData from(
            Long productId,
            Integer quantity
    ) {
        return new OrderItemData(productId, quantity);
    }
}