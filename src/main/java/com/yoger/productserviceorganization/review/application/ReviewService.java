package com.yoger.productserviceorganization.review.application;

import com.yoger.productserviceorganization.review.application.command.CreateReviewCommand;
import com.yoger.productserviceorganization.review.domain.model.Review;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewService {
    Review saveReview(CreateReviewCommand createReviewCommand, List<MultipartFile> images);

    List<Review> getReviewsByProductId(Long productId);

    void deleteReview(Long id, Long userId);

    Review updateReview(Long id, Long userId, Review newReview);
}
