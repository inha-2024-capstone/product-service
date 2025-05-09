package com.yoger.productserviceorganization;

import com.yoger.productserviceorganization.product.adapters.web.dto.response.ProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleProductResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

class TestApplicationUtil {
    private final WebTestClient webTestClient;

    TestApplicationUtil(WebTestClient webClient) {
        this.webTestClient = webClient;
    }

    MultipartBodyBuilder getTestBodyBuilder() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("name", "Test Product");
        builder.part("price", 1000);
        builder.part("description", "This is a test product description");
        builder.part("image", new ClassPathResource("test-image.jpeg"))
                .filename("test-image.jpeg")
                .contentType(MediaType.IMAGE_JPEG);
        builder.part("thumbnailImage", new ClassPathResource("test-thumbnail.jpeg"))
                .filename("test-thumbnail.jpeg")
                .contentType(MediaType.IMAGE_JPEG);
        builder.part("creatorName", "Test Creator");  // 제작자 이름 추가
        builder.part("dueDate", LocalDateTime.now().plusDays(30).toString());
        builder.part("stockQuantity", 100000);
        return builder;
    }

    List<SimpleProductResponseDTO> getSimpleTestProducts() {
        return webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SimpleProductResponseDTO.class)  // body를 List로 매핑
                .returnResult()
                .getResponseBody();
    }

    ProductResponseDTO getTestProduct(Long productId) {
        return webTestClient.get()
                .uri("/api/products/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .returnResult()
                .getResponseBody();
    }

    ProductResponseDTO registerTestProduct(Long creatorId, MultipartBodyBuilder builder) {
        return webTestClient.post()
                .uri("/api/products/register")
                .header("User-Id", String.valueOf(creatorId))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)  // DTO로 응답 본문을 매핑
                .returnResult()
                .getResponseBody();
    }
}
