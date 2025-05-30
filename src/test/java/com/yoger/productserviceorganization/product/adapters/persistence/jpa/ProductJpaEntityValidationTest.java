package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.yoger.productserviceorganization.product.domain.model.ProductState;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProductJpaEntityValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 ProductEntity 생성 - 검증 통과")
    void validProductEntityCreation() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "유효한상품이름",
                1000,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L,
                "제작자 이름",
                null, // dueDate
                100 // stockQuantity
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).isEmpty(); // 유효한 경우, 제약 위반이 없어야 함
    }

    @Test
    @DisplayName("상품 이름이 짧을 경우 - 검증 실패")
    void productNameTooShort() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "짧",
                1000,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L,
                "제작자 이름",
                null,
                100
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("상품 이름은 2글자 이상 50글자 이하만 가능합니다.");
    }

    @Test
    @DisplayName("상품 이름에 허용되지 않은 특수 문자가 포함된 경우 - 검증 실패")
    void productNameWithInvalidCharacters() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "잘못된#상품이름!",
                1000,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L,
                "제작자 이름",
                null,
                100
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("상품 이름은 한글, 영어, 숫자, '-', '_' 만 사용할 수 있습니다.");
    }

    @Test
    @DisplayName("상품 설명이 너무 짧을 경우 - 검증 실패")
    void productDescriptionTooShort() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "유효한상품이름",
                1000,
                "짧음",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L,
                "제작자 이름",
                null,
                100
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("상품 상세 설명은 10글자 이상 500글자 이하만 가능합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 S3 URL일 경우 - 검증 실패")
    void productInvalidS3Url() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "유효한상품이름",
                1000,
                "상품에 대한 설명입니다.",
                "https://invalid-url.com/image.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L,
                "제작자 이름",
                null,
                100
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("유효한 S3 URL 형식이어야 합니다.");
    }

    @Test
    @DisplayName("ProductState가 null일 경우 - 검증 실패")
    void productStateNull() {
        ProductJpaEntity productEntity = ProductJpaEntity.of(
                null,
                "유효한상품이름",
                1000,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                null,
                1L,
                "제작자 이름",
                null,
                100
        );

        Set<ConstraintViolation<ProductJpaEntity>> violations = validator.validate(productEntity);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("상품의 상태를 정해주세요.");
    }
}
