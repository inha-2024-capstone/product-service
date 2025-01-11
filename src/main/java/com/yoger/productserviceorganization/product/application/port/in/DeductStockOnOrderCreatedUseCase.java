package com.yoger.productserviceorganization.product.application.port.in;

public interface DeductStockOnOrderCreatedUseCase {
    void deductStock(DeductStockCommands deductStockCommands);
}
