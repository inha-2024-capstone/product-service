package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrdersUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.mapper.EventMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderCreatedEventConsumer {
    private final EventDeduplicateService eventDeduplicateService;
    private final DeductStockFromOrdersUseCase deductStockFromOrdersUseCase;

    @KafkaListener(
            topics = "${event.topic.order.created}",
            containerFactory = "kafkaOrderCreatedEventListenerContainerFactory"
    )
    public void consumeOrderCreatedEventBatch(List<OrderCreatedEvent> events, Acknowledgment ack) {
        try {
            List<OrderCreatedEvent> filteredEvents = filterDeduplicatedEvents(events);

            Map<Long, List<DeductStockCommandFromOrder>> groupedCommands =
                    toGroupedDeductCommands(filteredEvents);

            for (Map.Entry<Long, List<DeductStockCommandFromOrder>> entry : groupedCommands.entrySet()) {
                DeductStockBatchCommandFromOrder commandBatch =
                        new DeductStockBatchCommandFromOrder(entry.getKey(), entry.getValue());
                deductStockFromOrdersUseCase.deductStockFromOrders(commandBatch);
            }

            filteredEvents.forEach(e -> eventDeduplicateService.putKey(e.eventId()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to process order events", e);
        } finally {
            ack.acknowledge();
        }
    }

    private List<OrderCreatedEvent> filterDeduplicatedEvents(List<OrderCreatedEvent> events) {
        Set<String> seen = new HashSet<>();
        return events.stream()
                .filter(e -> seen.add(e.eventId())) // 중복된 이벤트 ID 제거 (같은 배치 내에서)
                .filter(e -> !eventDeduplicateService.isDuplicate(e.eventId())) // Redis 등으로 전체 중복 제거
                .toList();
    }

    private Map<Long, List<DeductStockCommandFromOrder>> toGroupedDeductCommands(List<OrderCreatedEvent> events) {
        return events.stream()
                .map(EventMapper::toCommand)
                .collect(Collectors.groupingBy(cmd -> cmd.getDeductStockCommand().getProductId()));
    }
}
