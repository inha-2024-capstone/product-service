package com.yoger.productserviceorganization.product.application.port.out;

import com.yoger.productserviceorganization.product.domain.model.Product;
import java.util.List;

public interface LoadProductPort {
    List<Product> loadProducts();

    Product loadProduct(Long productId);

    Product loadProductWithLock(Long productId);

    List<Product> loadProductsWithLock(List<Long> idsSorted);
}
