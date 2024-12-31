package com.yoger.productserviceorganization.review.adapter.persistence.jpa;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReviewEntity r WHERE r.id = :id")
    Optional<ReviewEntity> findByIdWithLock(@Param("id") Long id);

    List<ReviewEntity> findAllByProductId(Long productId);
}
