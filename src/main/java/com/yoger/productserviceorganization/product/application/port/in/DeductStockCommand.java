package com.yoger.productserviceorganization.product.application.port.in;

import java.time.LocalDateTime;

public record DeductStockCommand(
        Integer quantity,
        LocalDateTime occurrenceDateTime
) {
    public static DeductStockCommand of(
            Integer quantity,
            LocalDateTime occurrenceDateTime
    ) {
        return new DeductStockCommand(quantity, occurrenceDateTime);
    }
}
