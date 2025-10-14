package com.yoger.productserviceorganization.product.domain.event;

import java.time.LocalDateTime;

public record OutboxEvent(
        String id,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        LocalDateTime createdAt,
        String tracingSpanContext
) {
    public static OutboxEvent of(
            String id,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime createdAt,
            String tracingSpanContext
    ) {
        return new OutboxEvent(id, "product", aggregateId, eventType, payload, createdAt, tracingSpanContext);
    }

    // 주문 단위 이벤트용 헬퍼
    public static OutboxEvent ofOrder(
            String id,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime createdAt,
            String tracingSpanContext
    ) {
        return new OutboxEvent(id, "order", aggregateId, eventType, payload, createdAt, tracingSpanContext);
    }
}
