package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.IncreaseStockCommand;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IncreaseStockService implements IncreaseStockUseCase {
    private final LoadProductPort loadProductPort;
    private final PersistProductPort persistProductPort;

    @Override
    public void increaseStock(IncreaseStockCommand command) {
        Product product = loadProductPort.loadProductWithLock(command.productId());
        product.increaseStockQuantity(command.quantity());
        persistProductPort.persist(product);
    }
}
