package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import java.util.Objects;

final class OutboxEventMapper {

    private OutboxEventMapper() { }

    static OutboxEventJpaEntity toEntity(final OutboxEvent domain) {
        Objects.requireNonNull(domain, "domain must not be null");
        return OutboxEventJpaEntity.of(
                domain.id(),
                domain.aggregateType(),
                domain.aggregateId(),
                domain.eventType(),
                domain.payload(),
                domain.createdAt(),
                domain.tracingSpanContext()
        );
    }
}
