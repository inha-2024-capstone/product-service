package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionCompletedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {
    @Value("${event.topic.product.deductionCompleted}")
    private String DEDUCTION_COMPLETED_TOPIC;

    @Value("${event.topic.product.deductionFailed}")
    private String DEDUCTION_FAILED_TOPIC;

    private final KafkaTemplate<String, Object> producerKafkaTemplate;

    public void sendDeductionCompletedEvent(DeductionCompletedEvent event) {
        producerKafkaTemplate.send(DEDUCTION_COMPLETED_TOPIC, event.eventId(), event);
    }

    public void sendDeductionFailedEvent(DeductionFailedEvent event) {
        producerKafkaTemplate.send(DEDUCTION_FAILED_TOPIC, event.eventId(), event);
    }
}
