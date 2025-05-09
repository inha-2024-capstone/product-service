package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.GetProductQuery;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.domain.model.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProductService implements GetProductQuery {

    private final LoadProductPort loadProductPort;

    @Override
    public List<Product> getProducts() {
        return loadProductPort.loadProducts();
    }

    @Override
    public Product getProduct(Long productId) {
        return loadProductPort.loadProduct(productId);
    }
}
