package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommands;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockOnOrderCreatedUseCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {
    private final DeductStockOnOrderCreatedUseCase deductStockOnOrderCreatedUseCase;

    @KafkaListener(
            topics = "#{'${event.topic.order.created}'}",
            containerFactory = "kafkaOrderCreatedEventListenerContainerFactory"
    )
    public void consumeOrderCreatedEventBatch(List<OrderCreatedEvent> events, Acknowledgment acknowledgment) {
        Map<Long, List<DeductStockCommand>> deductStockCommandMap =
                groupDeductStockCommandsByProductId(events);
        try {
            deductStockCommandMap.forEach(
                    (productId, deductStockCommandList) -> deductStockOnOrderCreatedUseCase.deductStock(
                            DeductStockCommands.of(productId, deductStockCommandList)
                    )
            );
            acknowledgment.acknowledge();  // 메시지 처리 후 커밋
        } catch (Exception e) {
            throw new RuntimeException("Failed to process order events", e);
        }
    }

    private Map<Long, List<DeductStockCommand>> groupDeductStockCommandsByProductId(List<OrderCreatedEvent> events) {
        Map<Long, List<DeductStockCommand>> deductStockCommandMap = new HashMap<>();
        events.forEach(orderCreatedEvent -> {
            Long productId = orderCreatedEvent.data().productId();
            List<DeductStockCommand> deductStockCommands = deductStockCommandMap.computeIfAbsent(productId,
                    k -> new ArrayList<>());

            deductStockCommands.add(
                    DeductStockCommand.of(
                            orderCreatedEvent.orderId(),
                            orderCreatedEvent.data().orderQuantity(),
                            orderCreatedEvent.occurrenceDateTime()
                    )
            );
        });
        return deductStockCommandMap;
    }
}
