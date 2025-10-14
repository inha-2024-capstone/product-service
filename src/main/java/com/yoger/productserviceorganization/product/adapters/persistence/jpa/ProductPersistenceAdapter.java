package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.domain.exception.ProductNotFoundException;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ProductPersistenceAdapter implements LoadProductPort, PersistProductPort {
    private static final String PRODUCT_CACHE = "product:";
    private static final String PRODUCT_LIST_CACHE_BY_STATE = "productsByState:";

    private final JpaProductRepository jpaProductRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @PersistenceContext
    private final EntityManager entityManager;

    private void evictCacheForState(ProductState state) {
        String cacheKey = PRODUCT_LIST_CACHE_BY_STATE + state.name();
        redisTemplate.delete(cacheKey);
    }

    @Override
    public List<Product> loadProducts() {
        ProductState state = ProductState.SELLABLE;
        String cacheKey = PRODUCT_LIST_CACHE_BY_STATE + state.name();

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.get(0) instanceof Product) {
                return (List<Product>) cachedList;
            }
        }

        List<ProductJpaEntity> entities = jpaProductRepository.findByState(state);
        List<Product> products = entities.stream()
                .map(ProductMapper::toDomainFrom)
                .toList();

        redisTemplate.opsForValue().set(cacheKey, products, Duration.ofMinutes(5));
        return products;
    }

    @Override
    public Product loadProduct(Long productId) {
        return findProductWithCache(productId);
    }

    @Override
    public Product loadProductWithLock(Long productId) {
        ProductJpaEntity entity = jpaProductRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductMapper.toDomainFrom(entity);
    }

    @Override
    public Product persist(Product product) {
        ProductJpaEntity savedEntity = jpaProductRepository.save(ProductMapper.toEntityFrom(product));
        Product savedProduct = ProductMapper.toDomainFrom(savedEntity);

        String cacheKey = PRODUCT_CACHE + savedProduct.getId();
        redisTemplate.opsForValue().set(cacheKey, savedProduct, Duration.ofMinutes(5));

        evictCacheForState(savedProduct.getState());

        return savedProduct;
    }

    private Product findProductWithCache(Long productId) {
        String cacheKey = PRODUCT_CACHE + productId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Product product) {
            return product;
        }

        ProductJpaEntity entity = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        Product product = ProductMapper.toDomainFrom(entity);

        redisTemplate.opsForValue().set(cacheKey, product, Duration.ofMinutes(5));
        return product;
    }

    @Override
    public List<Product> loadProductsWithLock(List<Long> idsSorted) {
        if (idsSorted == null || idsSorted.isEmpty()) {
            return List.of();
        }
        // ORDER BY로 잠금 순서를 고정
        List<ProductJpaEntity> entities = entityManager.createQuery(
                        "SELECT p FROM ProductJpaEntity p WHERE p.id IN :ids ORDER BY p.id ASC",
                        ProductJpaEntity.class
                )
                .setParameter("ids", idsSorted)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        return entities.stream()
                .map(ProductMapper::toDomainFrom)
                .toList();
    }
}
