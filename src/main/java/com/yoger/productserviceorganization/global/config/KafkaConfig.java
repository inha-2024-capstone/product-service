package com.yoger.productserviceorganization.global.config;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import java.util.HashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfig {
    private final KafkaProducerProperties producerProps;
    private final KafkaConsumerProperties consumerProps;

    public KafkaConfig(
            KafkaProducerProperties producerProps,
            KafkaConsumerProperties consumerProps
    ) {
        this.producerProps = producerProps;
        this.consumerProps = consumerProps;
    }

    // Producer Config
    @Bean
    public HashMap<String, Object> producerConfig() {
        HashMap<String, Object> config = new HashMap<>();

        // 기본 프로듀서 설정
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerProps.bootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerProps.keySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerProps.valueSerializer());
        config.putAll(producerProps.properties());  // 커스텀 프로퍼티 합침

        return config;
    }

    // KafkaTemplate
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        DefaultKafkaProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(producerConfig());
        return new KafkaTemplate<>(producerFactory);
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
}
