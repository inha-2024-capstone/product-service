package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox")
@Getter
@AllArgsConstructor
@NoArgsConstructor
class OutboxEventJpaEntity {

    @Id
    private String id;

    @Column(name = "tracingspancontext", columnDefinition = "TEXT", nullable = false)
    private String tracingSpanContext;

    @Column(name = "aggregate_type")
    private String aggregateType;

    @Column(name = "aggregateid")
    private String aggregateId;

    @Column(name = "event_type")
    private String eventType;

    @Lob
    @Column(name = "payload", columnDefinition = "LONGTEXT", nullable = false)
    private String payload;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static OutboxEventJpaEntity of(
            final String id,
            final String aggregateType,
            final String aggregateId,
            final String eventType,
            final String payload,
            final LocalDateTime createdAt,
            final String tracingSpanContext
    ) {
        OutboxEventJpaEntity entity = new OutboxEventJpaEntity();
        entity.id = id;
        entity.aggregateType = aggregateType;
        entity.aggregateId = aggregateId;
        entity.eventType = eventType;
        entity.payload = payload;
        entity.createdAt = createdAt;
        entity.tracingSpanContext = tracingSpanContext;
        return entity;
    }
}