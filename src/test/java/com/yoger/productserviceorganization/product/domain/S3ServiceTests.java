package com.yoger.productserviceorganization.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.yoger.productserviceorganization.LocalStackS3Config;
import com.yoger.productserviceorganization.product.config.AwsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@SpringBootTest(
        classes = LocalStackS3Config.class
)
@ActiveProfiles("integration")
@Testcontainers
public class S3ServiceTests {
    @Autowired
    private S3Client s3TestClient;

    @Autowired
    private AwsProperties awsProperties;

    private S3Service s3Service;
    private static final String TEST_IMAGE_NAME = "test-image.jpeg";
    private static final String TEST_IMAGE_PATH = "test-image.jpeg";
    private static final String EXPECTED_URL_PATTERN = "https://test-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/[a-f0-9\\-]+_test-image\\.jpeg";

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3TestClient, awsProperties);
        // 버킷을 미리 생성
        s3TestClient.createBucket(CreateBucketRequest.builder().bucket(awsProperties.bucket()).build());
    }

    @Test
    void testUploadImage() throws Exception {
        ClassPathResource resource = new ClassPathResource(TEST_IMAGE_PATH);
        byte[] imageBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                TEST_IMAGE_NAME,
                MediaType.IMAGE_JPEG_VALUE,
                imageBytes
        );

        // 이미지 업로드 테스트
        String imageUrl = s3Service.uploadImage(mockFile);

        // URL 패턴 검증
        assertThat(imageUrl).matches(EXPECTED_URL_PATTERN);

        // 업로드된 이미지 파일의 키를 추출 (URL에서 마지막 부분)
        String key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // S3에 업로드한 파일이 실제로 있는지 확인
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperties.bucket())
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> objectResponseStream = s3TestClient.getObject(getObjectRequest);
        byte[] uploadedImageBytes = objectResponseStream.readAllBytes();

        // 파일 크기 검증
        assertThat(uploadedImageBytes.length).isEqualTo(imageBytes.length);
        // S3에서 가져온 이미지의 MIME 타입은 확인할 수 없지만, 업로드한 파일의 크기를 검증할 수 있음
        assertThat(uploadedImageBytes).isEqualTo(imageBytes);
    }
}
