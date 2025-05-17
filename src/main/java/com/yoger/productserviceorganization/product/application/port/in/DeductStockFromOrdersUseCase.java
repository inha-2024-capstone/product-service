package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;

public interface DeductStockFromOrdersUseCase {
    void deductStockFromOrders(DeductStockBatchCommandFromOrder deductStockBatchCommandFromOrder);
}
