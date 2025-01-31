package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

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
public class OutboxEvent {
    @Id
    private String id;

    private String aggregate_type;
    private String aggregateid;
    private String event_type;

    @Lob
    private String payload; // DeductionCompletedEvent JSON

    private LocalDateTime created_at;

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