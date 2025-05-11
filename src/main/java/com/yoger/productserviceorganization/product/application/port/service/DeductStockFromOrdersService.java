package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrdersUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockBatchCommandFromOrder;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.application.port.out.SaveOutboxEventPort;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.model.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeductStockFromOrdersService implements DeductStockFromOrdersUseCase {
    private final LoadProductPort loadProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PersistProductPort persistProductPort;
    private final OutboxEventFactory outboxEventFactory;

    @Override
    public void deductStockFromOrders(DeductStockBatchCommandFromOrder batchCommand) {
        Product product = loadProductPort.loadProductWithLock(batchCommand.getProductId());
        List<OutboxEvent> outboxEvents = processDeductStockCommands(product, batchCommand);

        saveOutboxEventPort.saveAll(outboxEvents);

        persistProductPort.persist(product);
    }

    private List<OutboxEvent> processDeductStockCommands(
            Product product,
            DeductStockBatchCommandFromOrder batchCommand
    ) {
        List<OutboxEvent> outboxEvents = new ArrayList<>();

        for (DeductStockCommandFromOrder command : batchCommand.getDeductStockCommands()) {
            int orderQuantity = command.getDeductStockCommand().getQuantity();
            try {
                product.deductStockQuantity(orderQuantity);
                outboxEvents.add(outboxEventFactory.createDeductionCompletedEvent(command));
            } catch (InsufficientStockException e) {
                outboxEvents.add(outboxEventFactory.createDeductionFailedEvent(command));
            }
        }
        return outboxEvents;
    }
}
