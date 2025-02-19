package com.yoger.productserviceorganization.product.application.port.out;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEvent;
import java.util.List;

public interface OutboxRepository {
    void saveAll(List<OutboxEvent> events);

    void save(OutboxEvent event);

    void deleteAll();
}
