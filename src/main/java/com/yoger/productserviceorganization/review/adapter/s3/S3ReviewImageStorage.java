package com.yoger.productserviceorganization.review.adapter.s3;

import com.yoger.productserviceorganization.review.config.AwsReviewProperties;
import com.yoger.productserviceorganization.review.application.port.out.ReviewImageStorage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Profile("aws")
public class S3ReviewImageStorage implements ReviewImageStorage {
    private final S3Client s3ReviewClient;
    private final AwsReviewProperties awsReviewProperties;

    @Override
    public List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(this::uploadImage)
                .toList();
    }

    private String uploadImage(MultipartFile image) {
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsReviewProperties.bucket())
                    .key(fileName)
                    .contentType(image.getContentType())
                    .contentLength(image.getSize())
                    .build();

            s3ReviewClient.putObject(putObjectRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", awsReviewProperties.bucket(), awsReviewProperties.region(), fileName);
        } catch (S3Exception e) {
            throw new RuntimeException("S3에 파일 업로드 중 오류 발생: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImages(List<String> imageUrls) {
        List<String> keysToDelete = imageUrls.stream()
                .map(this::collectKeyToDelete)
                .toList();
        try {
            // batch delete
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(awsReviewProperties.bucket())
                    .delete(Delete.builder()
                            .objects(keysToDelete.stream()
                                    .map(key -> ObjectIdentifier.builder().key(key).build())
                                    .toList())
                            .build())
                    .build();
            s3ReviewClient.deleteObjects(deleteRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("S3에서 파일 삭제 중 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String collectKeyToDelete(String imageUrl) {
        String bucket = awsReviewProperties.bucket();
        String region = awsReviewProperties.region();
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);

        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("잘못된 이미지 URL입니다: " + imageUrl);
        }

        return imageUrl.substring(prefix.length());
    }
}
