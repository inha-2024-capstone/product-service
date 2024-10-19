package com.yoger.productserviceorganization.product.adapters.s3;

import com.yoger.productserviceorganization.product.config.AwsProperties;
import com.yoger.productserviceorganization.product.domain.port.ImageStorageService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3ImageStorageService implements ImageStorageService {
    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    @Override
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

    @Override
    public void deleteImage(String imageUrl) {
        // S3 버킷 이름과 리전을 기반으로 URL에서 키를 추출
        String bucket = awsProperties.bucket();
        String region = awsProperties.region();
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("잘못된 이미지 URL입니다: " + imageUrl);
        }

        // URL에서 키 추출
        String key = imageUrl.substring(prefix.length());
        try {
            // 객체의 존재 여부 확인
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

            // 객체가 존재하면 삭제
            if (headObjectResponse != null) {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } else {
                throw new RuntimeException("파일이 존재하지 않습니다: " + imageUrl);
            }
        } catch (S3Exception e) {
            throw new RuntimeException("S3에서 파일 삭제 중 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }
}