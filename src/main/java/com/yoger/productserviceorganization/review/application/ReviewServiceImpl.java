package com.yoger.productserviceorganization.review.application;

import com.yoger.productserviceorganization.review.application.command.CreateReviewCommand;
import com.yoger.productserviceorganization.review.domain.model.Review;
import com.yoger.productserviceorganization.review.application.port.out.ReviewImageStorage;
import com.yoger.productserviceorganization.review.application.port.out.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageStorage reviewImageStorage;

    @Override
    @Transactional
    public Review saveReview(CreateReviewCommand createReviewCommand, List<MultipartFile> images) {
        List<String> imageUrls = reviewImageStorage.uploadImages(images);
        registerTransactionSynchronizationForImageDeletion(imageUrls);

        // userService restAPI 호출
        String userName="test";
        Review review = createReviewCommand.toReview(userName, imageUrls);
        return reviewRepository.save(review);
    }

    private void registerTransactionSynchronizationForImageDeletion(List<String> imageUrls) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    deleteUploadedImages(imageUrls);
                }
            }
        });
    }

    private void deleteUploadedImages(List<String> imageUrls) {
        reviewImageStorage.deleteImages(imageUrls);
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findReviewsByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findByIdWithLock(id);
        if(!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 삭제는 자신의 리뷰일 때만 가능합니다.");
        }
        reviewRepository.deleteReview(id);
    }

    @Override
    @Transactional
    public Review updateReview(Long id, Long userId, Review newReview) {
        Review originReview = reviewRepository.findByIdWithLock(id);
        if(!originReview.getUserId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 수정은 자신의 리뷰일 때만 가능합니다.");
        }

        return null;
    }
}
