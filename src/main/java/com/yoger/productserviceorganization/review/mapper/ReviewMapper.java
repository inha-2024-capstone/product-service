package com.yoger.productserviceorganization.review.mapper;

import com.yoger.productserviceorganization.review.adapter.persistence.jpa.ReviewEntity;
import com.yoger.productserviceorganization.review.adapter.web.dto.request.ReviewSaveRequestDTO;
import com.yoger.productserviceorganization.review.application.command.CreateReviewCommand;
import com.yoger.productserviceorganization.review.domain.model.Review;

public class ReviewMapper {
    public static CreateReviewCommand toSaveCommand(Long productId, Long userId, ReviewSaveRequestDTO reviewSaveRequestDTO) {
        return CreateReviewCommand.of(
                productId,
                userId,
                reviewSaveRequestDTO.starRating(),
                reviewSaveRequestDTO.content()
        );
    }

    public static Review toDomain(ReviewEntity reviewEntity) {
        return Review.of(
                reviewEntity.getId(),
                reviewEntity.getProductId(),
                reviewEntity.getUserId(),
                reviewEntity.getUserName(),
                reviewEntity.getStarRating(),
                reviewEntity.getImageUrls(),
                reviewEntity.getContent(),
                reviewEntity.getCreatedTime()
        );
    }

    public static ReviewEntity toEntity(Review review) {
        return ReviewEntity.of(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getUserName(),
                review.getStarRating(),
                review.getImageUrls(),
                review.getContent()
        );
    }
}
