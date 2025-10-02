package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrdersUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.application.port.out.SaveOutboxEventPort;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.model.Product;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeductStockFromOrdersService implements DeductStockFromOrdersUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PersistProductPort persistProductPort;
    private final OutboxEventFactory outboxEventFactory;

    @Override
    public void deductStockFromOrders(
            final DeductStockBatchCommandFromOrder batch,
            final List<String> tracingSpanContexts
    ) {
        final Product product = loadProductPort.loadProductWithLock(batch.getProductId());

        final List<DeductStockCommandFromOrder> commands = batch.getDeductStockCommands();
        validateCommandAndTracingSize(commands, tracingSpanContexts);

        final List<OutboxEvent> outboxEvents =
                buildOutboxEvents(product, commands, tracingSpanContexts);

        saveOutboxEventPort.saveAll(outboxEvents);
        persistProductPort.persist(product);
    }

    private static void validateCommandAndTracingSize(
            final List<DeductStockCommandFromOrder> commands,
            final List<String> tracingSpanContexts
    ) {
        if (commands.size() != tracingSpanContexts.size()) {
            throw new IllegalArgumentException(
                    "commands.size != tracingSpanContexts.size: " +
                            commands.size() + " vs " + tracingSpanContexts.size()
            );
        }
    }

    private List<OutboxEvent> buildOutboxEvents(
            final Product product,
            final List<DeductStockCommandFromOrder> commands,
            final List<String> tracingSpanContexts
    ) {
        final List<OutboxEvent> outboxEvents = new ArrayList<>(commands.size());
        for (int i = 0; i < commands.size(); i++) {
            final DeductStockCommandFromOrder command = commands.get(i);
            final String tracingProps = tracingSpanContexts.get(i);
            final OutboxEvent event = processSingleCommand(product, command, tracingProps);
            outboxEvents.add(event);
        }
        return outboxEvents;
    }

    private OutboxEvent processSingleCommand(
            final Product product,
            final DeductStockCommandFromOrder command,
            final String tracingSpanContext
    ) {
        try {
            final int quantity = command.getDeductStockCommand().getQuantity();
            product.deductStockQuantity(quantity);
            return outboxEventFactory.createDeductionCompletedEvent(command, tracingSpanContext);
        } catch (final InsufficientStockException ex) {
            return outboxEventFactory.createDeductionFailedEvent(command, tracingSpanContext);
        }
    }
}
