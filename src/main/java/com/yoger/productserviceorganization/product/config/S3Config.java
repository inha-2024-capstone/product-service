package com.yoger.productserviceorganization.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("!integration")  // 테스트 환경에서 제외
public class S3Config {
    private final AwsProperties awsProperties;

    public S3Config(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials
                .create(awsProperties.accessKey(), awsProperties.secretKey());
        return S3Client.builder()
                .region(Region.of(awsProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
