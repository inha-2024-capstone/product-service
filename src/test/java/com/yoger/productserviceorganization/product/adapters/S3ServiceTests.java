package com.yoger.productserviceorganization.product.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yoger.productserviceorganization.config.LocalStackS3Config;
import com.yoger.productserviceorganization.config.RedisTestConfig;
import com.yoger.productserviceorganization.product.adapters.s3.S3ManageProductImageAdapter;
import com.yoger.productserviceorganization.product.config.AwsProductProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@SpringBootTest(
        classes = {
                LocalStackS3Config.class,
                RedisTestConfig.class
        }
)
@ActiveProfiles({"integration", "aws"})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class S3ServiceTests {
    @Autowired
    private S3Client s3TestClient;

    private static final String TEST_IMAGE_NAME = "test-image.jpeg";
    private static final String TEST_IMAGE_PATH = "test-image.jpeg";
    private static final String EXPECTED_URL_PATTERN = "https://test-bucket\\.s3\\.ap-northeast-2\\.amazonaws\\.com/[a-f0-9\\-]+_test-image\\.jpeg";

    @Autowired
    private S3ManageProductImageAdapter s3ManageProductImageAdapter;

    @Autowired
    private AwsProductProperties awsProductProperties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    KafkaListenerEndpointRegistry registry;

    @BeforeAll
    void setUp() {
        // 버킷을 미리 생성
        s3TestClient.createBucket(CreateBucketRequest.builder().bucket(awsProductProperties.bucket()).build());
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE product_jpa_entity");
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
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
        String imageUrl = s3ManageProductImageAdapter.uploadImage(mockFile);

        // URL 패턴 검증
        assertThat(imageUrl).matches(EXPECTED_URL_PATTERN);

        // 업로드된 이미지 파일의 키를 추출 (URL에서 마지막 부분)
        String key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // S3에 업로드한 파일이 실제로 있는지 확인
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProductProperties.bucket())
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> objectResponseStream = s3TestClient.getObject(getObjectRequest);
        byte[] uploadedImageBytes = objectResponseStream.readAllBytes();

        // 파일 크기 검증
        assertThat(uploadedImageBytes.length).isEqualTo(imageBytes.length);
        // S3에서 가져온 이미지의 MIME 타입은 확인할 수 없지만, 업로드한 파일의 크기를 검증할 수 있음
        assertThat(uploadedImageBytes).isEqualTo(imageBytes);
    }

    @Test
    void testDeleteImage() throws Exception {
        // 1. 이미지 업로드
        ClassPathResource resource = new ClassPathResource(TEST_IMAGE_PATH);
        byte[] imageBytes = org.springframework.util.FileCopyUtils.copyToByteArray(resource.getInputStream());

        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                TEST_IMAGE_NAME,
                MediaType.IMAGE_JPEG_VALUE,
                imageBytes
        );

        String imageUrl = s3ManageProductImageAdapter.uploadImage(mockFile);

        // URL 패턴 검증
        assertThat(imageUrl).matches(EXPECTED_URL_PATTERN);

        // 업로드된 이미지 파일의 키를 추출 (URL에서 마지막 부분)
        String key = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // 2. 이미지가 S3에 존재하는지 확인
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProductProperties.bucket())
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> objectResponseStream = s3TestClient.getObject(getObjectRequest);
        byte[] uploadedImageBytes = objectResponseStream.readAllBytes();

        // 파일 크기 검증
        assertThat(uploadedImageBytes.length).isEqualTo(imageBytes.length);
        // 파일 내용 검증
        assertThat(uploadedImageBytes).isEqualTo(imageBytes);

        // 3. 이미지 삭제
        s3ManageProductImageAdapter.deleteImage(imageUrl);

        // 4. 이미지가 S3에서 삭제되었는지 확인
        assertThatThrownBy(() -> s3TestClient.getObject(getObjectRequest))
                .isInstanceOf(NoSuchKeyException.class)
                .hasMessageContaining("The specified key does not exist.");
    }

    @Test
    void testDeleteImageWithInvalidUrl() {
        String invalidUrl = "https://invalid-bucket.s3.ap-northeast-2.amazonaws.com/nonexistent-image.jpeg";

        assertThatThrownBy(() -> s3ManageProductImageAdapter.deleteImage(invalidUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("잘못된 이미지 URL입니다: " + invalidUrl);
    }

    @Test
    void testDeleteNonExistingImage() {
        String nonExistingImageUrl = String.format("https://%s.s3.%s.amazonaws.com/nonexistent-image.jpeg",
                awsProductProperties.bucket(), awsProductProperties.region());

        assertThatThrownBy(() -> s3ManageProductImageAdapter.deleteImage(nonExistingImageUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3에서 파일 삭제 중 오류 발생");
    }
}
