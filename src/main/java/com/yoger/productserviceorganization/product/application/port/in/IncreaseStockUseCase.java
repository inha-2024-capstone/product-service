package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.IncreaseStockCommand;

public interface IncreaseStockUseCase {
    void increaseStockFromOrderCanceled(IncreaseStockCommand command);
}
