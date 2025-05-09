package com.yoger.productserviceorganization.product.application.port.in.command;

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
