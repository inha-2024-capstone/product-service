package com.yoger.productserviceorganization.product.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionCompletedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionFailedEvent;
import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommandFromOrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {
    private final ObjectMapper objectMapper;

    public OutboxEvent createDeductionCompletedEvent(Long productId, DeductStockCommandFromOrderEvent command) {
        try {
            DeductionCompletedEvent domainEvent = DeductionCompletedEvent.from(productId, command);
            String payload = objectMapper.writeValueAsString(domainEvent);
            return OutboxEvent.of(
                    domainEvent.eventId(),
                    productId.toString(),
                    domainEvent.eventType(),
                    payload,
                    domainEvent.occurrenceDateTime()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DeductionCompletedEvent", e);
        }
    }

    public OutboxEvent createDeductionFailedEvent(Long productId, DeductStockCommandFromOrderEvent command) {
        try {
            DeductionFailedEvent domainEvent = DeductionFailedEvent.from(productId, command);
            String payload = objectMapper.writeValueAsString(domainEvent);
            return OutboxEvent.of(
                    domainEvent.eventId(),
                    productId.toString(),
                    domainEvent.eventType(),
                    payload,
                    domainEvent.occurrenceDateTime()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize DeductionFailedEvent", e);
        }
    }
}
