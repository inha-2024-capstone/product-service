package com.yoger.productserviceorganization.product.domain;

import com.yoger.productserviceorganization.product.config.AwsProperties;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public String uploadImage(MultipartFile image) {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsProperties.bucket())
                    .key(fileName)
                    .contentType(image.getContentType())
                    .contentLength(image.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", awsProperties.bucket(), awsProperties.region(), fileName);
        } catch (S3Exception e) {
            throw new RuntimeException("S3에 파일 업로드 중 오류 발생: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
