package com.yoger.productserviceorganization.product.application.port.in;

import java.time.LocalDateTime;

public record DeductStockCommand(
        Long orderId,
        Integer orderQuantity,
        LocalDateTime occurrenceDateTime
) {
    public static DeductStockCommand of(
            Long orderId,
            Integer orderQuantity,
            LocalDateTime occurrenceDateTime
    ) {
        return new DeductStockCommand(orderId, orderQuantity, occurrenceDateTime);
    }
}
