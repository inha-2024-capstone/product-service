package com.yoger.productserviceorganization.product.adapters.persistence;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.JpaOutboxRepository;
import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEvent;
import com.yoger.productserviceorganization.product.application.port.out.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {
    private final JpaOutboxRepository jpaOutboxRepository;

    @Override
    public void saveAll(List<OutboxEvent> events) {
        jpaOutboxRepository.saveAll(events);
    }

    @Override
    public void save(OutboxEvent event) {
        jpaOutboxRepository.save(event);
    }

    @Override
    public void deleteAll() {
        jpaOutboxRepository.deleteAll();
    }
}
