package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxRepository extends JpaRepository<OutboxEventJpaEntity, String> {
}
