package com.yoger.productserviceorganization.product.application.port.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionCompletedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionFailedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.OrderDeductionFailedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.OrderDeductionSucceededEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    // --- 기존 아이템 단위 ------------------------------------------------------

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

    // --- 신규: 주문 단위(집계형) ----------------------------------------------

    public OutboxEvent createOrderDeductionSucceededEvent(
            final DeductStockFromOrderCommand command,
            final String tracingSpanContext
    ) {
        final OrderDeductionSucceededEvent event =
                OrderDeductionSucceededEvent.from(command, tracingSpanContext);
        return createEvent(event);
    }

    public OutboxEvent createOrderDeductionFailedEvent(
            final DeductStockFromOrderCommand command,
            final String tracingSpanContext
    ) {
        final OrderDeductionFailedEvent event =
                OrderDeductionFailedEvent.from(command, tracingSpanContext);
        return createEvent(event);
    }

    // --- 공통 직렬화/매핑 -------------------------------------------------------

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
        if (domainEvent instanceof OrderDeductionSucceededEvent ok) {
            return toOutbox(ok, payload);
        }
        if (domainEvent instanceof OrderDeductionFailedEvent ng) {
            return toOutbox(ng, payload);
        }

        throw new IllegalArgumentException(
                "Unsupported event type: " + domainEvent.getClass().getName()
        );
    }

    // --- Mappers --------------------------------------------------------------

    private static OutboxEvent toOutbox(
            final DeductionCompletedEvent event,
            final String payload
    ) {
        return OutboxEvent.of(
                event.eventId(),
                String.valueOf(event.productId()),
                event.eventType().getValue(),
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
                event.eventType().getValue(),
                payload,
                event.occurrenceDateTime(),
                event.tracingSpanContext()
        );
    }

    private static OutboxEvent toOutbox(
            final OrderDeductionSucceededEvent event,
            final String payload
    ) {
        return OutboxEvent.ofOrder( // <- 주문 단위는 aggregateType=order
                event.eventId(),
                event.orderId(),
                event.eventType().getValue(),
                payload,
                event.occurrenceDateTime(),
                event.tracingSpanContext()
        );
    }

    private static OutboxEvent toOutbox(
            final OrderDeductionFailedEvent event,
            final String payload
    ) {
        return OutboxEvent.ofOrder(
                event.eventId(),
                event.orderId(),
                event.eventType().getValue(),
                payload,
                event.occurrenceDateTime(),
                event.tracingSpanContext()
        );
    }
}
