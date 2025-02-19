package com.yoger.productserviceorganization.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("aws")
@ConfigurationProperties(prefix = "cloud.aws.s3.product")
public record AwsProductProperties(
        String region,
        String bucket,
        String accessKey,
        String secretKey
) {
}
