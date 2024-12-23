package com.yoger.productserviceorganization.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("aws")
public class S3ProductConfig {
    private final AwsProductProperties awsProductProperties;

    public S3ProductConfig(AwsProductProperties awsProductProperties) {
        this.awsProductProperties = awsProductProperties;
    }

    @Bean
    public S3Client s3ProductClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create(awsProductProperties.accessKey(), awsProductProperties.secretKey());
        return S3Client.builder()
                .region(Region.of(awsProductProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
