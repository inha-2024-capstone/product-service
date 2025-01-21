package com.yoger.productserviceorganization.product.application.port.in;

public interface IncreaseStockUseCase {
    void increaseStockFromOrderCanceled(IncreaseStockCommand command);
}
