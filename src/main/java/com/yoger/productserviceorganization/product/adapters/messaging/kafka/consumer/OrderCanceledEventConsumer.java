package com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.dedup.EventDeduplicateService;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCanceledEvent;
import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockUseCase;
import com.yoger.productserviceorganization.product.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCanceledEventConsumer {
    private final EventDeduplicateService eventDeduplicateService;
    private final IncreaseStockUseCase increaseStockUseCase;

    @KafkaListener(topics = "${event.topic.order.canceled}")
    public void consumeOrderCanceledEvent(OrderCanceledEvent event) {
        // TODO: 해당 이벤트가 재고를 증가해야하는지 아닌지 검증 필요, 수동 커밋 전환 필요
        if (eventDeduplicateService.isDuplicate(event.eventId())) {
            return;
        }
        increaseStockUseCase.increaseStock(
                EventMapper.toCommand(event)
        );
    }
}
