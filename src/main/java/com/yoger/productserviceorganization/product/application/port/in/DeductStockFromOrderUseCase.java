package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;

public interface DeductStockFromOrderUseCase {

    void deduct(DeductStockFromOrderCommand command, String tracingProps);

}
