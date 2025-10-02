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

    public OutboxEvent createDeductionCompletedEvent(
            final DeductStockCommandFromOrder command,
            final String tracingSpanContext
    ) {
        final DeductionCompletedEvent event =
                DeductionCompletedEvent.from(
                        command.getDeductStockCommand().getProductId(),
                        command,
                        tracingSpanContext
                );
        return createEvent(event);
    }

    public OutboxEvent createDeductionFailedEvent(
            final DeductStockCommandFromOrder command,
            final String tracingSpanContext
    ) {
        final DeductionFailedEvent event =
                DeductionFailedEvent.from(
                        command.getDeductStockCommand().getProductId(),
                        command,
                        tracingSpanContext
                );
        return createEvent(event);
    }

    private OutboxEvent createEvent(final Object domainEvent) {
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(domainEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize domain event: " + domainEvent.getClass().getSimpleName(), e
            );
        }

        if (domainEvent instanceof DeductionCompletedEvent completed) {
            return toOutbox(completed, payload);
        }
        if (domainEvent instanceof DeductionFailedEvent failed) {
            return toOutbox(failed, payload);
        }

        throw new IllegalArgumentException(
                "Unsupported event type: " + domainEvent.getClass().getName()
        );
    }

    // --- Mappers -------------------------------------------------------------

    private static OutboxEvent toOutbox(
            final DeductionCompletedEvent event,
            final String payload
    ) {
        return OutboxEvent.of(
                event.eventId(),
                String.valueOf(event.productId()),
                event.eventType(),
                payload,
                event.occurrenceDateTime(),
                event.tracingSpanContext()
        );
    }

    private static OutboxEvent toOutbox(
            final DeductionFailedEvent event,
            final String payload
    ) {
        return OutboxEvent.of(
                event.eventId(),
                String.valueOf(event.productId()),
                event.eventType(),
                payload,
                event.occurrenceDateTime(),
                event.tracingSpanContext()
        );
    }
}
