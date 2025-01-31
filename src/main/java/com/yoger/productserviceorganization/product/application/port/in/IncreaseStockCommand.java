package com.yoger.productserviceorganization.product.application.port.in;

import java.time.LocalDateTime;

public record IncreaseStockCommand(
        Long productId,
        Integer quantity,
        LocalDateTime occurrenceDateTime
) {
    public static IncreaseStockCommand of(
            Long productId,
            Integer quantity,
            LocalDateTime occurrenceDateTime
    ) {
        return new IncreaseStockCommand(productId, quantity, occurrenceDateTime);
    }
}
