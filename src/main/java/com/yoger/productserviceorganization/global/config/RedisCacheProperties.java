package com.yoger.productserviceorganization.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisCacheProperties(
        String host,
        Integer port,
        String password
) {
}
