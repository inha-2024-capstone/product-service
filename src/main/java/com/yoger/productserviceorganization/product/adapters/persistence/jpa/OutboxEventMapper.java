package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;

final class OutboxEventMapper {
    private OutboxEventMapper() {}

    static OutboxEventJpaEntity toEntity(OutboxEvent domain) {
        return OutboxEventJpaEntity.of(
                domain.id(),
                domain.aggregateType(),
                domain.aggregateId(),
                domain.eventType(),
                domain.payload(),
                domain.createdAt()
        );
    }
}
