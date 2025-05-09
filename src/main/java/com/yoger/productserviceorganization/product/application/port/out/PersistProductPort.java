package com.yoger.productserviceorganization.product.application.port.out;

import com.yoger.productserviceorganization.product.domain.model.Product;

public interface PersistProductPort {
    Product persist(Product product);
}
