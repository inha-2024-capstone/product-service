package com.yoger.productserviceorganization.review.application.command;

import com.yoger.productserviceorganization.review.domain.model.Review;
import java.util.List;

public record CreateReviewCommand(
        Long productId,
        Long userId,
        Integer starRating,
        String content
) {
    public static CreateReviewCommand of(Long productId, Long userId, Integer starRating, String content) {
        return new CreateReviewCommand(productId, userId, starRating, content);
    }

    public Review toReview(String userName, List<String> imageUrls) {
        return Review.of(
                null,
                productId,
                userId,
                userName,
                starRating,
                imageUrls,
                content,
                null
        );
    }
}
