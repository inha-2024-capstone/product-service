package com.yoger.productserviceorganization.config;

public class AwsProductTestProperties{
    private final String region;
    private final String bucket;
    private final String accessKey;
    private final String secretKey;

    public AwsProductTestProperties() {
        this.region = "ap-northeast-2";
        this.bucket = "test-bucket";
        this.accessKey = "test-access-key";
        this.secretKey = "test-secret-key";
    }

    public String getRegion() {
        return region;
    }

    public String getBucket() {
        return bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
