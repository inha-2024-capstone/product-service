package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;
import java.util.List;

public interface DeductStockFromOrdersUseCase {
    void deductStockFromOrders(DeductStockBatchCommandFromOrder batchCommand,
                               List<String> tracingSpanContexts);
}
