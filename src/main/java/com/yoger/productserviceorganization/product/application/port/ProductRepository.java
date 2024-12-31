package com.yoger.productserviceorganization.product.application.port;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import java.util.List;

public interface ProductRepository {
    Product findById(Long id);

    Product save(Product product);

    void updateForState(Product product, ProductState beforeState);

    List<Product> findByState(ProductState state);

    void deleteById(Long productId);

    Product findByIdWithLock(Long id);

    Integer updateStock(Long productId, Integer quantity);

    Boolean existsById(Long productId);

    List<Product> findByCreatorId(Long creatorId);
}
