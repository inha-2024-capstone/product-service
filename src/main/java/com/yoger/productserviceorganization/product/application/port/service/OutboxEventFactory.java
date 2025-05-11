package com.yoger.productserviceorganization.product.application.port.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionCompletedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionFailedEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEvent createDeductionCompletedEvent(DeductStockCommandFromOrder command) {
        return createEvent(
                DeductionCompletedEvent.from(command.getDeductStockCommand().getProductId(), command)
        );
    }

    public OutboxEvent createDeductionFailedEvent(DeductStockCommandFromOrder command) {
        return createEvent(
                DeductionFailedEvent.from(command.getDeductStockCommand().getProductId(), command)
        );
    }

    private OutboxEvent createEvent(Object domainEvent) {
        try {
            String payload = objectMapper.writeValueAsString(domainEvent);

            if (domainEvent instanceof DeductionCompletedEvent event) {
                return buildOutbox(event.eventId(), event.productId(), event.eventType(), payload, event.occurrenceDateTime());
            }
            if (domainEvent instanceof DeductionFailedEvent event) {
                return buildOutbox(event.eventId(), event.productId(), event.eventType(), payload, event.occurrenceDateTime());
            }

            throw new IllegalArgumentException("Unsupported event type: " + domainEvent.getClass().getSimpleName());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event: " + domainEvent.getClass().getSimpleName(), e);
        }
    }

    private OutboxEvent buildOutbox(String eventId, Long productId, String eventType, String payload, java.time.LocalDateTime time) {
        return OutboxEvent.of(eventId, productId.toString(), eventType, payload, time);
    }
}
