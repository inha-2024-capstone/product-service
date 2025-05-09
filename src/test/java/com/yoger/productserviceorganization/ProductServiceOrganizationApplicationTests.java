package com.yoger.productserviceorganization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import com.yoger.productserviceorganization.config.LocalStackS3Config;
import com.yoger.productserviceorganization.config.RedisTestConfig;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.ProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleProductResponseDTO;
import com.yoger.productserviceorganization.product.config.AwsProductProperties;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                LocalStackS3Config.class,
                RedisTestConfig.class
        }
)
@ActiveProfiles({"integration", "aws"})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceOrganizationApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private S3Client s3TestClient;

    private TestApplicationUtil applicationUtil;

    @Autowired
    private AwsProductProperties awsProductProperties;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @BeforeAll
    public void setUp() {
        // 버킷을 미리 생성
        s3TestClient.createBucket(CreateBucketRequest.builder().bucket(awsProductProperties.bucket()).build());
        this.applicationUtil = new TestApplicationUtil(webTestClient);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE product_jpa_entity");
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void whenPostRequestThenProductCreated() {
        // Given
        MultipartBodyBuilder builder = applicationUtil.getTestBodyBuilder();

        // When
        ProductResponseDTO productResponseDTO = applicationUtil.registerTestProduct(1L, builder);

        // Then
        String expectedImageUrlPattern = String.format(
                "https://%s\\.s3\\.%s\\.amazonaws\\.com/[a-f0-9\\-]+_test-image\\.jpeg",
                awsProductProperties.bucket(),
                awsProductProperties.region()
        );

        // 이미지 URL에 대한 별도 검증
        assertThat(productResponseDTO.imageUrl()).matches(expectedImageUrlPattern);

        // 나머지 필드 검증
        assertThat(productResponseDTO)
                .extracting("name", "description", "creatorId", "creatorName", "state")
                .containsExactly("Test Product", "This is a test product description", 1L, "Test Creator", ProductState.SELLABLE);
    }

    @Test
    void whenDemoGetRequestThenDemoListReturned() {
        // Given
        MultipartBodyBuilder builder = applicationUtil.getTestBodyBuilder();

        // First, create a demo product
        ProductResponseDTO productResponseDTO = applicationUtil.registerTestProduct(1L, builder);

        // When
        List<SimpleProductResponseDTO> productResponseDTOs = applicationUtil.getSimpleTestProducts();

        // Then
        assertThat(productResponseDTOs)
                .hasSize(1)
                .extracting(
                        SimpleProductResponseDTO::id,
                        SimpleProductResponseDTO::name,
                        SimpleProductResponseDTO::creatorName,
                        SimpleProductResponseDTO::state
                )
                .containsExactly(
                        tuple(
                                productResponseDTO.id(),
                                productResponseDTO.name(),
                                productResponseDTO.creatorName(),
                                productResponseDTO.state()
                        )
                );
    }
}
