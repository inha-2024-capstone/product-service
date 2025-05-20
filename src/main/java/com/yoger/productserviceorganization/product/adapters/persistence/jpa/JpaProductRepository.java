package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.domain.model.ProductState;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaProductRepository extends JpaRepository<ProductJpaEntity, Long> {
    List<ProductJpaEntity> findByState(ProductState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.id = :id")
    Optional<ProductJpaEntity> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductJpaEntity p SET p.stockQuantity = p.stockQuantity + :quantity " +
            "WHERE p.id = :id AND p.state = 'SELLABLE' AND p.stockQuantity + :quantity >= 0")
    Integer updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    List<ProductJpaEntity> findAllByCreatorId(Long creatorId);

    @Query("SELECT p.state FROM ProductJpaEntity p WHERE p.id = :id")
    Optional<ProductState> findStateById(@Param("id") Long id);
}
