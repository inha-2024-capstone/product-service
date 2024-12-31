package com.yoger.productserviceorganization.review.adapter.persistence.jpa;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "상품의 ID는 반드시 필요합니다.")
    private Long productId;

    @NotNull(message = "리뷰 작성자 ID는 반드시 필요합니다.")
    private Long userId;

    @NotBlank(message = "상품의 작성자 이름이 필요합니다.")
    private String userName;

    @NotNull(message = "별점은 빈값일 수 없습니다.")
    private Integer starRating;

    @Nullable
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> imageUrls;

    @NotBlank(message = "상품에 대한 설명을 추가해야합니다.")
    private String content;

    @CreatedDate
    private LocalDateTime createdTime;

    public static ReviewEntity of(
            Long id,
            Long productId,
            Long userId,
            String userName,
            Integer starRating,
            List<String> imageUrls,
            String content
    ) {
        return new ReviewEntity(id, productId, userId, userName, starRating, imageUrls, content, null);
    }
}
