package com.yoger.productserviceorganization.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public record KafkaConsumerProperties(
        @NotBlank
        String bootstrapServers,

        @NotBlank
        String keyDeserializer,

        @NotBlank
        String valueDeserializer,

        @NotBlank
        String groupId,

        boolean enableAutoCommit,

        @NotBlank
        String autoOffsetReset,

        @NotNull
        Map<String, Object> properties
) {
}