package com.yoger.productserviceorganization.product.application.port.in;

import java.util.List;

public record DeductStockCommands(
        // productId가 Key
        Long productId,
        List<DeductStockCommand> deductStockCommands
) {
    public static DeductStockCommands of(
            Long productId,
            List<DeductStockCommand> deductStockCommands
    ) {
        return new DeductStockCommands(productId, deductStockCommands);
    }
}
