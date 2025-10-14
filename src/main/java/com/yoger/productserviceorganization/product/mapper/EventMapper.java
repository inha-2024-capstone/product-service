package com.yoger.productserviceorganization.product.mapper;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.DeductStockFromOrderEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCanceledEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand.OrderItem;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.IncreaseStockCommand;
import java.util.List;

public final class EventMapper {
    private EventMapper() {}

    public static DeductStockCommandFromOrder toCommand(final OrderCreatedEvent event) {
        final DeductStockCommand deductCommand = new DeductStockCommand(
                event.data().productId(),
                event.data().orderQuantity(),
                event.occurrenceDateTime()
        );
        return new DeductStockCommandFromOrder(event.orderId(), deductCommand);
    }

    public static IncreaseStockCommand toCommand(final OrderCanceledEvent event){
        return new IncreaseStockCommand(
                event.data().productId(),
                event.data().orderQuantity(),
                event.occurrenceDateTime()
        );
    }

    public static DeductStockFromOrderCommand toDeductStockFromOrderCommand(
            final DeductStockFromOrderEvent event
    ) {
        final List<OrderItem> items = event.data().orderItems()
                .stream()
                .map(orderItemData -> new DeductStockFromOrderCommand.OrderItem(
                        orderItemData.productId(),
                        orderItemData.quantity()
                ))
                .toList();

        return new DeductStockFromOrderCommand(
                event.orderId(),
                event.eventId(),
                event.data().userId(),
                items,
                event.occurrenceDateTime()
        );
    }
}
