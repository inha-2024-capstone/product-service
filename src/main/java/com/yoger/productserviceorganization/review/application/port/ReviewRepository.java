package com.yoger.productserviceorganization.review.application.port;

import com.yoger.productserviceorganization.review.domain.model.Review;
import java.util.List;

public interface ReviewRepository {
    Review save(Review review);

    Review findByIdWithLock(Long id);

    List<Review> findReviewsByProductId(Long id);

    void deleteReview(Long id);

    Review updateReview(Long id, Review newReview);
}
