package com.yoger.productserviceorganization.product.application.port.in;

import java.util.List;

public record DeductStockCommandsFromOrderEvent(
        // productId가 Key
        Long productId,
        List<DeductStockCommandFromOrderEvent> deductStockCommands
) {
    public static DeductStockCommandsFromOrderEvent of(
            Long productId,
            List<DeductStockCommandFromOrderEvent> deductStockCommands
    ) {
        return new DeductStockCommandsFromOrderEvent(productId, deductStockCommands);
    }
}
