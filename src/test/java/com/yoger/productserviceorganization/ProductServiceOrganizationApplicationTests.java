package com.yoger.productserviceorganization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import com.yoger.productserviceorganization.product.config.AwsProperties;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.dto.response.DemoProductResponseDTO;
import com.yoger.productserviceorganization.product.dto.response.SimpleDemoProductResponseDTO;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = LocalStackS3Config.class
)
@ActiveProfiles("integration")
@Testcontainers
class ProductServiceOrganizationApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    private S3Client s3TestClient;

    @Autowired
    private AwsProperties awsProperties;

    @BeforeEach
    public void setUp() {
        // 버킷을 미리 생성
        s3TestClient.createBucket(CreateBucketRequest.builder().bucket(awsProperties.bucket()).build());
    }

    private MultipartBodyBuilder getTestBodyBuilder() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("name", "Test Product");
        builder.part("description", "This is a test product description");
        builder.part("image", new ClassPathResource("test-image.jpeg"))
                .filename("test-image.jpeg")
                .contentType(MediaType.IMAGE_JPEG);
        builder.part("thumbnailImage", new ClassPathResource("test-thumbnail.jpeg"))
                .filename("test-thumbnail.jpeg")
                .contentType(MediaType.IMAGE_JPEG);
        builder.part("creatorId", "1");  // Long 값으로 전송
        builder.part("creatorName", "Test Creator");  // 제작자 이름 추가
        return builder;
    }

    private List<SimpleDemoProductResponseDTO> getSimpleDemoTestProducts() {
        return webTestClient.get()
                .uri("/api/products/demo")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SimpleDemoProductResponseDTO.class)  // body를 List로 매핑
                .returnResult()
                .getResponseBody();
    }

    private DemoProductResponseDTO makeDemoTestProduct(MultipartBodyBuilder builder) {
        return webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DemoProductResponseDTO.class)  // DTO로 응답 본문을 매핑
                .returnResult()
                .getResponseBody();
    }

    @Test
    void whenPostRequestThenProductCreated() throws IOException {
        // Given
        MultipartBodyBuilder builder = getTestBodyBuilder();

        // When
        DemoProductResponseDTO demoProductResponseDTO = makeDemoTestProduct(builder);

        // Then
        String expectedImageUrlPattern = String.format(
                "https://%s\\.s3\\.%s\\.amazonaws\\.com/[a-f0-9\\-]+_test-image\\.jpeg",
                awsProperties.bucket(),
                awsProperties.region()
        );

        // 이미지 URL에 대한 별도 검증
        assertThat(demoProductResponseDTO.imageUrl()).matches(expectedImageUrlPattern);

        // 나머지 필드 검증
        assertThat(demoProductResponseDTO)
                .extracting(
                        DemoProductResponseDTO::name,
                        DemoProductResponseDTO::description,
                        DemoProductResponseDTO::creatorId,
                        DemoProductResponseDTO::creatorName,
                        DemoProductResponseDTO::state
                )
                .containsExactly(
                        "Test Product",
                        "This is a test product description",
                        1L,
                        "Test Creator",
                        ProductState.DEMO
                );
    }

    @Test
    void whenDemoGetRequestThenDemoListReturned() {
        // Given
        MultipartBodyBuilder builder = getTestBodyBuilder();

        // First, create a demo product
        DemoProductResponseDTO demoProductResponseDTO = makeDemoTestProduct(builder);

        // When
        List<SimpleDemoProductResponseDTO> demoProductResponseDTOs = getSimpleDemoTestProducts();

        // Then
        assertThat(demoProductResponseDTOs)
                .hasSize(1)
                .extracting(
                        SimpleDemoProductResponseDTO::id,
                        SimpleDemoProductResponseDTO::name,
                        SimpleDemoProductResponseDTO::creatorName,
                        SimpleDemoProductResponseDTO::state
                )
                .containsExactly(
                        tuple(
                                demoProductResponseDTO.id(),
                                demoProductResponseDTO.name(),
                                demoProductResponseDTO.creatorName(),
                                demoProductResponseDTO.state()
                        )
                );
    }
}

