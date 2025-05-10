package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandsFromOrderEvent;
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
public class StockDeductionService implements DeductStockUseCase {
    private final LoadProductPort loadProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PersistProductPort persistProductPort;
    private final OutboxEventFactory outboxEventFactory;

    @Override
    public void applyDeduction(DeductStockCommandsFromOrderEvent commands) {
        Product product = loadProductPort.loadProductWithLock(commands.productId());
        List<OutboxEvent> outboxEvents = processDeductStockCommands(product, commands);

        saveOutboxEventPort.saveAll(outboxEvents);

        persistProductPort.persist(product);
    }

    private List<OutboxEvent> processDeductStockCommands(
            Product product,
            DeductStockCommandsFromOrderEvent commands
    ) {
        List<OutboxEvent> outboxEvents = new ArrayList<>();

        for (DeductStockCommandFromOrderEvent commandEvent : commands.deductStockCommands()) {
            int orderQuantity = commandEvent.deductStockCommand().quantity();
            try {
                product.deductStockQuantity(orderQuantity);
                outboxEvents.add(outboxEventFactory.createDeductionCompletedEvent(product.getId(), commandEvent));
            } catch (InsufficientStockException e) {
                outboxEvents.add(outboxEventFactory.createDeductionFailedEvent(product.getId(), commandEvent));
            }
        }
        return outboxEvents;
    }
}
