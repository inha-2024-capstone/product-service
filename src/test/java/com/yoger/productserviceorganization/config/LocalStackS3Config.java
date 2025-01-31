package com.yoger.productserviceorganization.config;

import com.yoger.productserviceorganization.product.adapters.s3.S3ProductImageStorage;
import com.yoger.productserviceorganization.product.config.AwsProductProperties;
import com.yoger.productserviceorganization.review.adapter.s3.S3ReviewImageStorage;
import com.yoger.productserviceorganization.review.config.AwsReviewProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
@Profile("aws & integration")
public class LocalStackS3Config {
    private final AwsProductTestProperties awsProductTestProperties;
    private final AwsReviewTestProperties awsReviewTestProperties;

    public LocalStackS3Config() {
        this.awsProductTestProperties = new AwsProductTestProperties();
        this.awsReviewTestProperties = new AwsReviewTestProperties();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.S3)
                .withEnv("DEFAULT_REGION", awsProductTestProperties.getRegion());
    }

    @Bean
    public S3Client s3TestClient(LocalStackContainer container) {
        return S3Client.builder()
                .endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        container.getAccessKey(), container.getSecretKey())))
                .region(Region.of(container.getRegion()))
                .build();
    }

    @Bean
    public S3ProductImageStorage s3ProductImageStorage(S3Client s3TestClient) {
        return new S3ProductImageStorage(s3TestClient, awsProductProperties());
    }

    @Bean
    public S3ReviewImageStorage s3ReviewImageStorage(S3Client s3TestClient) {
        return new S3ReviewImageStorage(s3TestClient, awsReviewProperties());
    }

    @Bean
    public AwsProductProperties awsProductProperties() {
        return new AwsProductProperties(
                awsProductTestProperties.getRegion(),
                awsProductTestProperties.getBucket(),
                awsProductTestProperties.getAccessKey(),
                awsProductTestProperties.getSecretKey()
        );
    }

    @Bean
    public AwsReviewProperties awsReviewProperties() {
        return new AwsReviewProperties(
                awsReviewTestProperties.getRegion(),
                awsReviewTestProperties.getBucket(),
                awsReviewTestProperties.getAccessKey(),
                awsReviewTestProperties.getSecretKey()
        );
    }
}
