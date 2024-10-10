package com.yoger.productserviceorganization.product.persistence;

import com.yoger.productserviceorganization.product.domain.exception.ProductNotFoundException;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.mapper.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final JpaProductRepository jpaProductRepository;

    @Override
    public Product findById(Long productId) {
        ProductEntity productEntity = jpaProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductMapper.toDomainFrom(productEntity);
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductMapper.toEntityFrom(product);
        ProductEntity savedEntity = jpaProductRepository.save(productEntity);
        return ProductMapper.toDomainFrom(savedEntity);
    }

    @Override
    public List<Product> findByState(ProductState state) {
        return jpaProductRepository.findByState(state).stream()
                .map(ProductMapper::toDomainFrom)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return jpaProductRepository.findAll().stream()
                .map(ProductMapper::toDomainFrom)
                .toList();  //toList() 메서드는 불변 리스트를 반환 만약 변경 해야하는 상황이 발생하면 수정 필요
    }
}
