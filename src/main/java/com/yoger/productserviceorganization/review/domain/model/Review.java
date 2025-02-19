package com.yoger.productserviceorganization.review.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class Review {
    private final Long id;
    private final Long productId;
    private final Long userId;
    private final String userName;
    private final Integer starRating;
    private final List<String> imageUrls;
    private final String content;
    private final LocalDateTime createdTime;

    private Review(
            Long id,
            Long productId,
            Long userId,
            String userName,
            Integer starRating,
            List<String> imageUrls,
            String content,
            LocalDateTime createdTime
    ) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.starRating = starRating;
        this.imageUrls = imageUrls;
        this.content = content;
        this.createdTime = createdTime;
    }

    public static Review of(
            Long id,
            Long productId,
            Long userId,
            String userName,
            Integer starRating,
            List<String> imageUrls,
            String content,
            LocalDateTime createdTime
    ) {
        validate(starRating);
        return new Review(id, productId, userId, userName, starRating, imageUrls, content, createdTime);
    }

    private static void validate(Integer starRating) {
        if (starRating < 0 || starRating > 5) {
            throw new IllegalArgumentException("잘못된 별점 선택입니다.");
        }
    }
}
