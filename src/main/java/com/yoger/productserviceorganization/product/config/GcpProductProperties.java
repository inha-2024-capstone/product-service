package com.yoger.productserviceorganization.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("gcp")
@ConfigurationProperties(prefix = "cloud.gcp.gcs")
public record GcpProductProperties(
        String projectId,
        String bucket,
        String credentialsPath
) {
}
