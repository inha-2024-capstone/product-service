package com.yoger.productserviceorganization.review.adapter.persistence;

import com.yoger.productserviceorganization.review.adapter.persistence.jpa.JpaReviewRepository;
import com.yoger.productserviceorganization.review.adapter.persistence.jpa.ReviewEntity;
import com.yoger.productserviceorganization.review.domain.exception.ReviewNotFoundException;
import com.yoger.productserviceorganization.review.domain.model.Review;
import com.yoger.productserviceorganization.review.application.port.out.ReviewRepository;
import com.yoger.productserviceorganization.review.mapper.ReviewMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {
    private static final String REVIEW_PREFIX = "review:product:";

    private final JpaReviewRepository jpaReviewRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Review save(Review review) {
        ReviewEntity reviewEntity = ReviewMapper.toEntity(review);
        ReviewEntity savedEntity = jpaReviewRepository.save(reviewEntity);

        String cacheKey = REVIEW_PREFIX + savedEntity.getProductId();
        redisTemplate.delete(cacheKey);

        return ReviewMapper.toDomain(savedEntity);
    }

    @Override
    public Review findByIdWithLock(Long id) {
        ReviewEntity reviewEntity = jpaReviewRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        return ReviewMapper.toDomain(reviewEntity);
    }

    @Override
    public List<Review> findReviewsByProductId(Long productId) {
        String cacheKey = REVIEW_PREFIX + productId;
        List<ReviewEntity> cachedEntities = (List<ReviewEntity>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntities != null) {
            return cachedEntities.stream()
                    .map(ReviewMapper::toDomain)
                    .toList();
        }
        List<ReviewEntity> reviewEntities = jpaReviewRepository.findAllByProductId(productId);

        redisTemplate.opsForValue().set(cacheKey, reviewEntities);
        return reviewEntities.stream()
                .map(ReviewMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteReview(Long id) {
        ReviewEntity reviewEntity = jpaReviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        jpaReviewRepository.deleteById(id);

        String cacheKey = REVIEW_PREFIX + reviewEntity.getProductId();
        redisTemplate.delete(cacheKey);
    }

    @Override
    public Review updateReview(Long id, Review newReview) {
        return null;
    }
}
