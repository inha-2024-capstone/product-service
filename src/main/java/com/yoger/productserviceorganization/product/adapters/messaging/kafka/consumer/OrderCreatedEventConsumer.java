package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.dedup.EventDeduplicateService;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandsFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {
    private final DeductStockUseCase deductStockUseCase;
    private final EventDeduplicateService eventDeduplicateService;

    @KafkaListener(
            topics = "${event.topic.order.created}",
            containerFactory = "kafkaOrderCreatedEventListenerContainerFactory"
    )
    public void consumeOrderCreatedEventBatch(List<OrderCreatedEvent> events, Acknowledgment acknowledgment) {
        List<OrderCreatedEvent> deduplicatedEvents = createDeduplicatedEvents(events);
        Map<Long, List<DeductStockCommandFromOrderEvent>> deductStockCommandMap =
                groupDeductStockCommandsByProductId(deduplicatedEvents);
        try {
            deductStockCommandMap.forEach(
                    (productId, deductStockCommandList) -> deductStockUseCase.deductStockFromOrderCreated(
                            DeductStockCommandsFromOrderEvent.of(productId, deductStockCommandList)
                    )
            );
            deduplicatedEvents.forEach(e -> eventDeduplicateService.putKey(e.eventId()));
            acknowledgment.acknowledge();  // 메시지 처리 후 커밋
        } catch (Exception e) {
            throw new RuntimeException("Failed to process order events", e);
        }
    }

    private List<OrderCreatedEvent> createDeduplicatedEvents(List<OrderCreatedEvent> events) {
        Set<String> encounteredIds = new HashSet<>();
        return events.stream()
                .filter(event -> encounteredIds.add(event.eventId()))
                .filter(event -> !eventDeduplicateService.isDuplicate(event.eventId()))
                .toList();
    }

    private Map<Long, List<DeductStockCommandFromOrderEvent>> groupDeductStockCommandsByProductId(List<OrderCreatedEvent> events) {
        Map<Long, List<DeductStockCommandFromOrderEvent>> deductStockCommandMap = new HashMap<>();
        events.forEach(orderCreatedEvent -> {
            Long productId = orderCreatedEvent.data().productId();
            List<DeductStockCommandFromOrderEvent> deductStockCommands = deductStockCommandMap.computeIfAbsent(
                    productId,
                    k -> new ArrayList<>()
            );

            deductStockCommands.add(
                    DeductStockCommandFromOrderEvent.of(
                            orderCreatedEvent.orderId(),
                            orderCreatedEvent.data().orderQuantity(),
                            orderCreatedEvent.occurrenceDateTime()
                    )
            );
        });
        return deductStockCommandMap;
    }
}
