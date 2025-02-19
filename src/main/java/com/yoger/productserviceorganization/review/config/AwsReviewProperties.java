package com.yoger.productserviceorganization.review.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("aws")
@ConfigurationProperties(prefix = "cloud.aws.s3.review")
public record AwsReviewProperties(
        String region,
        String bucket,
        String accessKey,
        String secretKey
) {
}
