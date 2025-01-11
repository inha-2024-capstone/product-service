package com.yoger.productserviceorganization.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.kafka.producer")
public record KafkaProducerProperties(
        @NotBlank
        String bootstrapServers,

        @NotBlank
        String keySerializer,

        @NotBlank
        String valueSerializer,

        @NotNull
        Map<String, Object> properties
) {
}
