package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import com.yoger.productserviceorganization.global.config.DataConfig;
import com.yoger.productserviceorganization.product.domain.model.PriceByQuantity;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
public class ProductRepositoryJpaTests {

    @Autowired
    private JpaProductRepository productRepository;

    private List<PriceByQuantity> priceByQuantities;

    @BeforeEach
    void setUp() {
        priceByQuantities = List.of(
                new PriceByQuantity(100, 10000)
                , new PriceByQuantity(1000, 8500)
                , new PriceByQuantity(10000, 7500)
        );
    }

    @Test
    @DisplayName("상품들이 정상적으로 저장되었는 지 테스트")
    void findAllProducts() {
        ProductEntity product1 = ProductEntity.of(
                null,
                "유효한상품이름1",
                priceByQuantities,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L, // creatorId
                "제작자 이름1", // creatorName
                LocalDateTime.now().plusDays(30), // dueDate
                100, // initialStockQuantity
                100 // stockQuantity
        );
        ProductEntity product2 = ProductEntity.of(
                null,
                "유효한상품이름2",
                priceByQuantities,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                2L, // creatorId
                "제작자 이름2", // creatorName
                LocalDateTime.now().plusDays(30), // dueDate
                200, // initialStockQuantity
                200 // stockQuantity
        );
        ProductEntity savedProduct1 = productRepository.save(product1);
        ProductEntity savedProduct2 = productRepository.save(product2);

        List<ProductEntity> actualProducts = productRepository.findAll();

        assertThat(actualProducts)
                .extracting(ProductEntity::getId)
                .contains(savedProduct1.getId(), savedProduct2.getId());
    }

    @ParameterizedTest
    @MethodSource("invalidProductParameters")
    @DisplayName("유효성 검증 실패 테스트 - 다양한 경우")
    void productValidationFailTest(String name, String description, String imageUrl, ProductState state,
                                   String thumbnailImageUrl, Long creatorId, String creatorName,
                                   String expectedMessage) {
        ProductEntity product = ProductEntity.of(
                null,
                name,
                priceByQuantities,
                description,
                imageUrl,
                thumbnailImageUrl,
                state,
                creatorId,
                creatorName,
                LocalDateTime.now().plusDays(30), // dueDate
                100, // initialStockQuantity
                100 // stockQuantity
        );

        assertThatThrownBy(() -> productRepository.save(product))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining(expectedMessage);
    }

    // 유효성 실패 케이스들을 제공하는 메서드
    private static Stream<Arguments> invalidProductParameters() {
        return Stream.of(
                // name 유효성 검증 실패 케이스들
                Arguments.of(null, "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품 이름을 작성해주세요."),
                Arguments.of("짧", "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품 이름은 2글자 이상 50글자 이하만 가능합니다."),
                Arguments.of("이름이 너무너무 길어요".repeat(10), "상품에 대한 설명입니다.",
                        "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg", ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품 이름은 2글자 이상 50글자 이하만 가능합니다."),
                Arguments.of("이름에 허용하지 않은 특수부호 #", "상품에 대한 설명입니다.",
                        "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg", ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품 이름은 한글, 영어, 숫자, '-', '_' 만 사용할 수 있습니다."),

                // description 유효성 검증 실패 케이스들
                Arguments.of("정상 이름", null, "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE, "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L,
                        "제작자 이름",
                        "상품에 대한 설명을 적어주세요."),
                Arguments.of("정상 이름", "짧", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE, "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L,
                        "제작자 이름",
                        "상품 상세 설명은 10글자 이상 500글자 이하만 가능합니다."),
                Arguments.of("정상 이름", "상품 설명이 너무 길어요".repeat(100),
                        "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg", ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품 상세 설명은 10글자 이상 500글자 이하만 가능합니다."),

                // imageUrl 유효성 검증 실패 케이스들
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", null, ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "상품에 대한 사진을 추가해주세요."),
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", "https://wrong-url.com/myimage.jpg", ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, "제작자 이름",
                        "유효한 S3 URL 형식이어야 합니다."),

                // thumbnailImageUrl 유효성 검증 실패 케이스들
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        null, 1L, "제작자 이름", "상품에 대한 대표 사진을 추가해주세요."),
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        "https://wrong-url.com/my-thumbnail.jpg", 1L, "제작자 이름", "유효한 S3 URL 형식이어야 합니다."),

                // creatorId 유효성 검증 실패 케이스들
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", null, "제작자 이름",
                        "제작자의 ID를 추가해주세요."),

                // creatorName 유효성 검증 실패 케이스들
                Arguments.of("정상 이름", "상품에 대한 설명입니다.", "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                        ProductState.SELLABLE,
                        "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg", 1L, null,
                        "제작자의 이름을 입력해주세요.")
        );
    }

    @Test
    @DisplayName("상품이 정상적으로 삭제되는지 테스트")
    void deleteProduct() {
        ProductEntity product = ProductEntity.of(
                1L,
                "유효한상품이름1",
                priceByQuantities,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L, // creatorId
                "제작자 이름1", // creatorName
                LocalDateTime.now().plusDays(30), // dueDate
                100, // initialStockQuantity
                100 // stockQuantity
        );
        ProductEntity savedProduct = productRepository.save(product);

        // Ensure product exists before deletion
        assertThat(savedProduct.getName()).isEqualTo(product.getName());

        // When: 상품을 삭제
        productRepository.deleteById(savedProduct.getId());

        assertThat(productRepository.existsById(savedProduct.getId())).isEqualTo(false);
    }
}
