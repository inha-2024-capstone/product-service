package com.yoger.productserviceorganization.product.mapper;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCanceledEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.IncreaseStockCommand;

public final class EventMapper {
    private EventMapper() {}

    public static DeductStockCommandFromOrder toCommand(OrderCreatedEvent event) {
        DeductStockCommand deductCommand = new DeductStockCommand(
                event.data().productId(),
                event.data().orderQuantity(),
                event.occurrenceDateTime()
        );
        return new DeductStockCommandFromOrder(event.orderId(), deductCommand);
    }

    public static IncreaseStockCommand toCommand(OrderCanceledEvent event){
        return new IncreaseStockCommand(
                event.data().productId(),
                event.data().orderQuantity(),
                event.occurrenceDateTime()
        );
    }
}
