package com.yoger.productserviceorganization.product.mapper;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEventJpaEntity;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;

public final class OutboxEventMapper {
    private OutboxEventMapper() {}

    public static OutboxEventJpaEntity toEntity(OutboxEvent domain) {
        return new OutboxEventJpaEntity(
                domain.id(),
                domain.aggregateType(),
                domain.aggregateId(),
                domain.eventType(),
                domain.payload(),
                domain.createdAt()
        );
    }
}
