package com.yoger.productserviceorganization.product.domain.event;

import java.time.LocalDateTime;

public record OutboxEvent(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        LocalDateTime createdAt
) {
    public static OutboxEvent of(
            String id,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime createdAt
    ) {
        return new OutboxEvent(id, "product", aggregateId, eventType, payload, createdAt);
    }
}
