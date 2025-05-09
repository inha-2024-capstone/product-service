package com.yoger.productserviceorganization.product.application.port.in.command;

import java.time.LocalDateTime;

public record DeductStockCommandFromOrderEvent(
        Long orderId,
        DeductStockCommand deductStockCommand
) {
    public static DeductStockCommandFromOrderEvent of(
            Long orderId,
            Integer quantity,
            LocalDateTime occurrenceDateTime
    ) {
        return new DeductStockCommandFromOrderEvent(orderId, DeductStockCommand.of(quantity, occurrenceDateTime));
    }
}
