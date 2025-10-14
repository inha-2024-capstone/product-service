package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.global.config.TraceUtil;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.DeductStockFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrderUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;
import com.yoger.productserviceorganization.product.mapper.EventMapper;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class DeductStockFromOrderEventConsumer {

    private final EventDeduplicateService eventDeduplicateService;
    private final DeductStockFromOrderUseCase deductStockFromOrderUseCase;

    @KafkaListener(
            topics = "${event.topic.order.created}",
            containerFactory = "kafkaDeductStockFromOrderEventListenerContainerFactory"
    )
    public void consumeDeductStockFromOrderEvent(
            final ConsumerRecord<String, DeductStockFromOrderEvent> record,
            final Acknowledgment ack
    ) {
        if (isSkippable(record)) {
            ack.acknowledge();
            return;
        }

        final DeductStockFromOrderEvent event = record.value();

        try (final Scope scope = TraceUtil.extractFromKafkaHeaders(record.headers()).makeCurrent()) {

            processMessage(event);

            handleSuccess(event, ack);

        } catch (Exception ex) {
            log.error("Failed to process DeductStockFromOrderEvent. eventId: {}, orderId: {}. Triggering retry/DLQ.",
                    event.eventId(), event.orderId(), ex);
            throw ex;
        }
    }

    private boolean isSkippable(final ConsumerRecord<String, DeductStockFromOrderEvent> record) {
        if (record.value() == null) {
            return true;
        }
        return eventDeduplicateService.isDuplicate(record.value().eventId());
    }

    private void processMessage(final DeductStockFromOrderEvent event) {
        final String tracingProps = TraceUtil.serializedTracingProperties();
        final DeductStockFromOrderCommand command = EventMapper.toDeductStockFromOrderCommand(event);

        deductStockFromOrderUseCase.deduct(command, tracingProps);
    }

    private void handleSuccess(final DeductStockFromOrderEvent event, final Acknowledgment ack) {
        eventDeduplicateService.putKey(event.eventId());
        ack.acknowledge();
    }
}
