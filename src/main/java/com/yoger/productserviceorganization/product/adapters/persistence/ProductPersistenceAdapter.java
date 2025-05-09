package com.yoger.productserviceorganization.product.adapters.persistence;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.JpaProductRepository;
import com.yoger.productserviceorganization.product.adapters.persistence.jpa.ProductJpaEntity;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.application.port.out.ProductRepository;
import com.yoger.productserviceorganization.product.domain.exception.ProductNotFoundException;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.mapper.ProductMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepository, LoadProductPort, PersistProductPort {
    private static final String PRODUCT_ENTITY_CACHE = "productEntity : ";
    private static final String PRODUCT_ENTITY_CACHE_BY_STATE = "productEntitiesByState : ";

    private final JpaProductRepository jpaProductRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void updateForState(Product product, ProductState beforeState) {
        ProductJpaEntity productEntity = ProductMapper.toEntityFrom(product);
        ProductJpaEntity savedEntity = jpaProductRepository.save(productEntity);
        String cacheKey = PRODUCT_ENTITY_CACHE + savedEntity.getId();
        redisTemplate.opsForValue().set(cacheKey, savedEntity, Duration.ofMinutes(5));

        evictCacheForState(beforeState);
        evictCacheForState(product.getState());
    }

    @Override
    public List<Product> findByState(ProductState state) {
        String cacheKey = PRODUCT_ENTITY_CACHE_BY_STATE + state.name();
        List<ProductJpaEntity> cachedEntities = (List<ProductJpaEntity>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntities != null) {
            return cachedEntities.stream()
                    .map(ProductMapper::toDomainFrom)
                    .toList();
        }

        List<ProductJpaEntity> productEntities = jpaProductRepository.findByState(state);

        redisTemplate.opsForValue().set(cacheKey, productEntities, Duration.ofMinutes(5));
        return productEntities.stream()
                .map(ProductMapper::toDomainFrom)
                .toList();
    }

    @Override
    public void deleteById(Long productId) {
        ProductState productState = jpaProductRepository.findStateById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        jpaProductRepository.deleteById(productId);

        String cacheKey = PRODUCT_ENTITY_CACHE + productId;
        redisTemplate.delete(cacheKey);

        evictCacheForState(productState);
    }

    @Override
    public Integer updateStock(Long productId, Integer quantity) {
        Integer result = jpaProductRepository.updateStock(productId, quantity);
        if (result > 0) {
            String cacheKey = PRODUCT_ENTITY_CACHE + productId;
            ProductJpaEntity updatedEntity = findEntityByIdWithCaching(productId);
            redisTemplate.opsForValue().set(cacheKey, updatedEntity, Duration.ofMinutes(5));
            // No need to evict all caches since stock update may not affect other cached data
        }
        return result;
    }

    @Override
    public List<Product> findByCreatorId(Long creatorId) {
        return jpaProductRepository.findAllByCreatorId(creatorId)
                .stream()
                .map(ProductMapper::toDomainFrom)
                .toList();
    }

    private void evictCacheForState(ProductState state) {
        String cacheKey = PRODUCT_ENTITY_CACHE_BY_STATE + state.name();
        redisTemplate.delete(cacheKey);
    }

    @Override
    public List<Product> loadProducts() {
        ProductState state = ProductState.SELLABLE;
        String cacheKey = PRODUCT_ENTITY_CACHE_BY_STATE + state.name();
        List<ProductJpaEntity> cachedEntities = (List<ProductJpaEntity>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntities != null) {
            return cachedEntities.stream()
                    .map(ProductMapper::toDomainFrom)
                    .toList();
        }

        List<ProductJpaEntity> productEntities = jpaProductRepository.findByState(state);

        redisTemplate.opsForValue().set(cacheKey, productEntities, Duration.ofMinutes(5));

        return productEntities.stream()
                .map(ProductMapper::toDomainFrom)
                .toList();
    }

    @Override
    public Product loadProduct(Long productId) {
        return ProductMapper.toDomainFrom(findEntityByIdWithCaching(productId));
    }

    @Override
    public Product loadProductWithLock(Long productId) {
        ProductJpaEntity productEntity = jpaProductRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductMapper.toDomainFrom(productEntity);
    }

    @Override
    public Product persist(Product product) {
        ProductJpaEntity productEntity = ProductMapper.toEntityFrom(product);
        ProductJpaEntity savedEntity = jpaProductRepository.save(productEntity);
        String cacheKey = PRODUCT_ENTITY_CACHE + savedEntity.getId();
        redisTemplate.opsForValue().set(cacheKey, savedEntity, Duration.ofMinutes(5));

        evictCacheForState(savedEntity.getState());

        return ProductMapper.toDomainFrom(savedEntity);
    }

    private ProductJpaEntity findEntityByIdWithCaching(Long productId) {
        String cacheKey = PRODUCT_ENTITY_CACHE + productId;
        ProductJpaEntity cachedEntity = (ProductJpaEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntity != null) {
            return cachedEntity;
        }
        ProductJpaEntity productEntity = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        redisTemplate.opsForValue().set(cacheKey, productEntity, Duration.ofMinutes(5));
        return productEntity;
    }
}
