package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandsFromOrderEvent;

public interface DeductStockUseCase {
    void deductStock(DeductStockCommandsFromOrderEvent deductStockCommandsFromOrderEvent);
}
