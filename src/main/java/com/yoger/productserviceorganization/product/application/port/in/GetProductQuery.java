package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.domain.model.Product;
import java.util.List;

public interface GetProductQuery {
    List<Product> getProducts();

    Product getProduct(Long productId);
}
