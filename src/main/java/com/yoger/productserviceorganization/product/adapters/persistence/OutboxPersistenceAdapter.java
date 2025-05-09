package com.yoger.productserviceorganization.product.adapters.persistence;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.JpaOutboxRepository;
import com.yoger.productserviceorganization.product.application.port.out.ClearOutboxEventPort;
import com.yoger.productserviceorganization.product.application.port.out.SaveOutboxEventPort;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import com.yoger.productserviceorganization.product.mapper.OutboxEventMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements SaveOutboxEventPort, ClearOutboxEventPort {
    private final JpaOutboxRepository jpaOutboxRepository;

    @Override
    public void save(OutboxEvent event) {
        jpaOutboxRepository.save(OutboxEventMapper.toEntity(event));
    }

    @Override
    public void saveAll(List<OutboxEvent> events) {
        jpaOutboxRepository.saveAll(events.stream().map(
                        OutboxEventMapper::toEntity
                ).toList()
        );
    }

    @Override
    public void clear() {
        jpaOutboxRepository.deleteAll();
    }
}
