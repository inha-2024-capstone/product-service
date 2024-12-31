package com.yoger.productserviceorganization.product.adapters.persistence;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.JpaProductRepository;
import com.yoger.productserviceorganization.product.adapters.persistence.jpa.ProductEntity;
import com.yoger.productserviceorganization.product.application.port.ProductRepository;
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
public class ProductRepositoryImpl implements ProductRepository {
    private static final String PRODUCT_ENTITY_CACHE = "productEntity : ";
    private static final String PRODUCT_ENTITY_CACHE_BY_STATE = "productEntitiesByState : ";
    private static final String PRODUCT_ENTITY_STOCK = "productEntityStock : ";

    private final JpaProductRepository jpaProductRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Product findById(Long productId) {
        return ProductMapper.toDomainFrom(findEntityByIdWithCaching(productId));
    }

    private ProductEntity findEntityByIdWithCaching(Long productId) {
        String cacheKey = PRODUCT_ENTITY_CACHE + productId;
        ProductEntity cachedEntity = (ProductEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntity != null) {
            return cachedEntity;
        }
        ProductEntity productEntity = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        redisTemplate.opsForValue().set(cacheKey, productEntity, Duration.ofMinutes(5));
        return productEntity;
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductMapper.toEntityFrom(product);
        ProductEntity savedEntity = jpaProductRepository.save(productEntity);
        String cacheKey = PRODUCT_ENTITY_CACHE + savedEntity.getId();
        redisTemplate.opsForValue().set(cacheKey, savedEntity, Duration.ofMinutes(5));

        evictCacheForState(savedEntity.getState());

        return ProductMapper.toDomainFrom(savedEntity);
    }

    @Override
    public void updateForState(Product product, ProductState beforeState) {
        ProductEntity productEntity = ProductMapper.toEntityFrom(product);
        ProductEntity savedEntity = jpaProductRepository.save(productEntity);
        String cacheKey = PRODUCT_ENTITY_CACHE + savedEntity.getId();
        redisTemplate.opsForValue().set(cacheKey, savedEntity, Duration.ofMinutes(5));

        evictCacheForState(beforeState);
        evictCacheForState(product.getState());
    }

    @Override
    public List<Product> findByState(ProductState state) {
        String cacheKey = PRODUCT_ENTITY_CACHE_BY_STATE + state.name();
        List<ProductEntity> cachedEntities = (List<ProductEntity>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedEntities != null) {
            return cachedEntities.stream()
                    .map(ProductMapper::toDomainFrom)
                    .toList();
        }

        List<ProductEntity> productEntities = jpaProductRepository.findByState(state);

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
    public Product findByIdWithLock(Long productId) {
        ProductEntity productEntity = jpaProductRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductMapper.toDomainFrom(productEntity);
    }

    @Override
    public Integer updateStock(Long productId, Integer quantity) {
        Integer result = jpaProductRepository.updateStock(productId, quantity);
        if (result > 0) {
            String cacheKey = PRODUCT_ENTITY_CACHE + productId;
            ProductEntity updatedEntity = findEntityByIdWithCaching(productId);
            redisTemplate.opsForValue().set(cacheKey, updatedEntity, Duration.ofMinutes(5));
            // No need to evict all caches since stock update may not affect other cached data
        }
        return result;
    }

    @Override
    public Boolean existsById(Long productId) {
        return jpaProductRepository.existsById(productId);
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
}
