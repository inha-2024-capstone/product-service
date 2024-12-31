package com.yoger.productserviceorganization.review.adapter.web.controller;

import com.yoger.productserviceorganization.review.adapter.web.dto.request.ReviewSaveRequestDTO;
import com.yoger.productserviceorganization.review.adapter.web.dto.response.ReviewResponseDTO;
import com.yoger.productserviceorganization.review.application.ReviewService;
import com.yoger.productserviceorganization.review.application.command.CreateReviewCommand;
import com.yoger.productserviceorganization.review.domain.model.Review;
import com.yoger.productserviceorganization.review.mapper.ReviewMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProductId(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(
                reviews.stream()
                        .map(ReviewResponseDTO::from)
                        .toList()
        );
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ReviewResponseDTO> saveReview(
            @PathVariable Long productId,
            @RequestHeader(value = "User-Id") Long userId,
            @Valid @ModelAttribute ReviewSaveRequestDTO reviewSaveRequestDTO
    ) {
        CreateReviewCommand createReviewCommand = ReviewMapper.toSaveCommand(productId, userId, reviewSaveRequestDTO);
        Review savedReview = reviewService.saveReview(createReviewCommand, reviewSaveRequestDTO.images());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponseDTO.from(savedReview));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader(value = "User-Id") Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
