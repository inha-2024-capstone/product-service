package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.global.config.TraceUtil;
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

    @Column(name="tracingspancontext")
    private String tracingSpanContext;

    private String aggregate_type;

    private String aggregateid;

    private String event_type;

    @Lob
    private String payload; // DeductionCompletedEvent JSON

    private LocalDateTime created_at;

    private OutboxEventJpaEntity(
            String id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.tracingSpanContext = TraceUtil.serializedTracingProperties();
        this.aggregate_type = aggregateType;
        this.aggregateid = aggregateId;
        this.event_type = eventType;
        this.payload = payload;
        this.created_at = createdAt;
    }

    public static OutboxEventJpaEntity of(
            String id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime createdAt
    ) {
        return new OutboxEventJpaEntity(id, aggregateType, aggregateId, eventType, payload, createdAt);
    }
}