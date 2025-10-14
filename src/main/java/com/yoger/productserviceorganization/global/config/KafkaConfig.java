package com.yoger.productserviceorganization.global.config;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.DeductStockFromOrderEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import java.util.HashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfig {
    private final KafkaConsumerProperties consumerProps;

    public KafkaConfig(
            KafkaConsumerProperties consumerProps
    ) {
        this.consumerProps = consumerProps;
    }

    // Consumer Config
    @Bean
    public HashMap<String, Object> consumerConfig() {
        HashMap<String, Object> config = new HashMap<>();

        // 기본 컨슈머 설정
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerProps.bootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProps.keyDeserializer());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProps.groupId());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProps.enableAutoCommit());
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProps.autoOffsetReset());
        config.putAll(consumerProps.properties());

        return config;
    }

    // KafkaListener Container Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaOrderCreatedEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // ConsumerFactory 생성
        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerConfig(),
                        new StringDeserializer(),
                        new JsonDeserializer<>(OrderCreatedEvent.class, false)
                );

        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true); // 배치 모드
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeductStockFromOrderEvent>
    kafkaDeductStockFromOrderEventListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DeductStockFromOrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        DefaultKafkaConsumerFactory<String, DeductStockFromOrderEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        consumerConfig(),
                        new StringDeserializer(),
                        new JsonDeserializer<>(DeductStockFromOrderEvent.class, false) // 헤더 타입정보 미사용
                );

        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(false); // 단건 처리
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 수동 즉시 커밋

        return factory;
    }
}
