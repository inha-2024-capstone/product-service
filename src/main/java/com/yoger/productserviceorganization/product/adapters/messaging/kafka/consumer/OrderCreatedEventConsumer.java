package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.global.config.TraceUtil;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrdersUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.mapper.EventMapper;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderCreatedEventConsumer {

    private final EventDeduplicateService eventDeduplicateService;
    private final DeductStockFromOrdersUseCase deductStockFromOrdersUseCase;

    // 중복키 저장 및 트레이싱 전달을 위한 래퍼
    public record CommandWithTracing(
            String eventId,
            DeductStockCommandFromOrder command,
            String tracingSpanContext
    ) {}

    @KafkaListener(
            topics = "${event.topic.order.created.v2}",
            containerFactory = "kafkaOrderCreatedEventListenerContainerFactory",
            autoStartup = "${event.listener.order-created.enabled:false}"
    )
    public void consumeOrderCreatedEventBatch(
            final List<ConsumerRecord<String, OrderCreatedEvent>> records,
            final Acknowledgment ack
    ) {
        if (records == null || records.isEmpty()) {
            ack.acknowledge();
            return;
        }

        final List<CommandWithTracing> prepared = prepareCommands(records);
        if (prepared.isEmpty()) {
            ack.acknowledge();
            return;
        }

        final Map<Long, List<CommandWithTracing>> grouped = groupByProductId(prepared);
        processGroupedBatches(grouped);

        // 성공 처리 후 전역 중복키 저장
        prepared.forEach(ct -> eventDeduplicateService.putKey(ct.eventId()));

        ack.acknowledge();
    }

    private List<CommandWithTracing> prepareCommands(
            final List<ConsumerRecord<String, OrderCreatedEvent>> records
    ) {
        final HashSet<String> seenInBatch = new HashSet<>();
        final List<CommandWithTracing> result = new ArrayList<>();

        for (final ConsumerRecord<String, OrderCreatedEvent> record : records) {
            final OrderCreatedEvent event = record.value();
            if (event == null) {
                continue;
            }
            if (!seenInBatch.add(event.eventId())) {         // 배치 내 중복 제거
                continue;
            }
            if (eventDeduplicateService.isDuplicate(event.eventId())) { // 전역 중복 제거
                continue;
            }

            // 레코드별 스코프: 이 블록 내의 current()를 직렬화하여 per-record 트레이싱 보존
            try (final Scope scope =
                         TraceUtil.extractFromKafkaHeaders(record.headers()).makeCurrent()) {
                final DeductStockCommandFromOrder cmd = EventMapper.toCommand(event);
                final String tracingProps = TraceUtil.serializedTracingProperties();
                result.add(new CommandWithTracing(event.eventId(), cmd, tracingProps));
            }
        }
        return result;
    }

    private Map<Long, List<CommandWithTracing>> groupByProductId(
            final List<CommandWithTracing> prepared
    ) {
        return prepared.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.command().getDeductStockCommand().getProductId()
                ));
    }

    private void processGroupedBatches(final Map<Long, List<CommandWithTracing>> grouped) {
        for (final Map.Entry<Long, List<CommandWithTracing>> entry : grouped.entrySet()) {
            final Long productId = entry.getKey();
            final List<DeductStockCommandFromOrder> commands = entry.getValue().stream()
                    .map(CommandWithTracing::command)
                    .toList();
            final List<String> tracingContexts = entry.getValue().stream()
                    .map(CommandWithTracing::tracingSpanContext)
                    .toList();

            deductStockFromOrdersUseCase.deductStockFromOrders(
                    new DeductStockBatchCommandFromOrder(productId, commands),
                    tracingContexts
            );
        }
    }
}
