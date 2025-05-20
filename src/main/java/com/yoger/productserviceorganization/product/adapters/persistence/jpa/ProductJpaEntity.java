package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.domain.model.ProductState;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
class ProductJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "상품 이름을 작성해주세요.")
    @Size(min = 2, max = 50, message = "상품 이름은 2글자 이상 50글자 이하만 가능합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9\\-\\_ ]+$",
            message = "상품 이름은 한글, 영어, 숫자, '-', '_' 만 사용할 수 있습니다."
    )
    private String name;

    @NotNull(message = "가격은 필수 항목입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @NotBlank(message = "상품에 대한 설명을 적어주세요.")
    @Size(min = 10, max = 500, message = "상품 상세 설명은 10글자 이상 500글자 이하만 가능합니다.")
    private String description;

    @NotBlank(message = "상품에 대한 사진을 추가해주세요.")
    @Pattern(
            regexp = "^(https?)://[a-zA-Z0-9\\-]+\\.s3\\.[a-zA-Z0-9\\-]+\\.amazonaws\\.com/.*$",
            message = "유효한 S3 URL 형식이어야 합니다."
    )
    private String imageUrl;

    @NotBlank(message = "상품에 대한 대표 사진을 추가해주세요.")
    @Pattern(
            regexp = "^(https?)://[a-zA-Z0-9\\-]+\\.s3\\.[a-zA-Z0-9\\-]+\\.amazonaws\\.com/.*$",
            message = "유효한 S3 URL 형식이어야 합니다."
    )
    private String thumbnailImageUrl;

    @NotNull(message = "상품의 상태를 정해주세요.")
    @Enumerated(EnumType.STRING)
    private ProductState state;

    @NotNull(message = "제작자의 ID를 추가해주세요.")
    private Long creatorId;

    @NotBlank(message = "제작자의 이름을 입력해주세요.")
    private String creatorName;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Nullable
    private LocalDateTime dueDate;

    private int stockQuantity;

    /*
     * @Version을 통한 낙관적 락킹을 할 수 있지만,
     * Product의 경우에는 다수의 사용자가 동일한 상품을 수정하는 경우는
     * 존재하지 않기 때문에 작성하지 않음
     */
    public static ProductJpaEntity of(
            Long id,
            String name,
            Integer price,
            String description,
            String imageUrl,
            String thumbnailImageUrl,
            ProductState state,
            Long creatorId,
            String creatorName ,
            LocalDateTime dueDate,
            int stockQuantity
    ) {
        return new ProductJpaEntity(
                id,
                name,
                price,
                description,
                imageUrl,
                thumbnailImageUrl,
                state,
                creatorId,
                creatorName,
                null,
                null,
                dueDate,
                stockQuantity
        );
    }
}
