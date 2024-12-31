package com.yoger.productserviceorganization.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("!integration & aws")
public class S3ReviewConfig {
    private final AwsReviewProperties awsReviewProperties;

    public S3ReviewConfig(AwsReviewProperties awsReviewProperties) {
        this.awsReviewProperties = awsReviewProperties;
    }

    @Bean
    public S3Client s3ReviewClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create(awsReviewProperties.accessKey(), awsReviewProperties.secretKey());
        return S3Client.builder()
                .region(Region.of(awsReviewProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
