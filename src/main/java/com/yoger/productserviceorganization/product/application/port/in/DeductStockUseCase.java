package com.yoger.productserviceorganization.product.application.port.in;

public interface DeductStockUseCase {
    void deductStockFromOrderCreated(DeductStockCommandsFromOrderEvent deductStockCommandsFromOrderEvent);
}
