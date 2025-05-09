package com.yoger.productserviceorganization.product.application.port.out;

import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import java.util.List;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);

    void saveAll(List<OutboxEvent> events);
}
