package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.ProductRepository;
import com.yoger.productserviceorganization.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockIncreaseService implements IncreaseStockUseCase {
    private final ProductRepository productRepository;

    @Override
    public void applyIncrease(IncreaseStockCommand command) {
        Product product = productRepository.findByIdWithLock(command.productId());
        product.increaseStockQuantity(command.quantity());
        productRepository.save(product);
    }
}
