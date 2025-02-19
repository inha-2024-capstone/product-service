package com.yoger.productserviceorganization.review.adapter.web.dto.response;

import com.yoger.productserviceorganization.review.domain.model.Review;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDTO(
        Long id,
        String userName,
        Integer starRating,
        List<String> imageUrls,
        String content,
        LocalDateTime createdTime
) {
    public static ReviewResponseDTO from(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getUserName(),
                review.getStarRating(),
                review.getImageUrls(),
                review.getContent(),
                review.getCreatedTime()
        );
    }
}
