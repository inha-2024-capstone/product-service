package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.command.IncreaseStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockCommandsFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.SaveOutboxEventPort;
import com.yoger.productserviceorganization.product.domain.event.OutboxEvent;
import com.yoger.productserviceorganization.product.domain.model.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockChangeService implements DeductStockUseCase, IncreaseStockUseCase {
    private final LoadProductPort loadProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PersistProductPort persistProductPort;
    private final OutboxEventFactory outboxEventFactory;

    private record DeductionResult(int totalOrderQuantity, List<OutboxEvent> outboxEvents) {}

    @Override
    public void deductStockFromOrderCreated(DeductStockCommandsFromOrderEvent commands) {
        Long productId = commands.productId();
        Product product = loadProductPort.loadProductWithLock(productId);
        int currentStockQuantity = product.getStockQuantity();
        DeductionResult deductionResult = processDeductStockCommands(commands, productId, currentStockQuantity);

        saveOutboxEventPort.saveAll(deductionResult.outboxEvents);

        product.deductStockQuantity(deductionResult.totalOrderQuantity);
        persistProductPort.persist(product);
    }

    private DeductionResult processDeductStockCommands(
            DeductStockCommandsFromOrderEvent commands,
            Long productId,
            int currentStockQuantity
    ) {
        int totalOrderQuantity = 0;
        List<OutboxEvent> outboxEvents = new ArrayList<>();

        for (DeductStockCommandFromOrderEvent commandEvent : commands.deductStockCommands()) {
            int orderQuantity = commandEvent.deductStockCommand().quantity();
            if (canDeduct(currentStockQuantity, totalOrderQuantity, orderQuantity)) {
                totalOrderQuantity += orderQuantity;
                outboxEvents.add(outboxEventFactory.createDeductionCompletedEvent(productId, commandEvent));
            } else {
                outboxEvents.add(outboxEventFactory.createDeductionFailedEvent(productId, commandEvent));
            }
        }
        return new DeductionResult(totalOrderQuantity, outboxEvents);
    }

    private boolean canDeduct(int currentStockQuantity, int totalOrderQuantity, int orderQuantity) {
        return totalOrderQuantity + orderQuantity <= currentStockQuantity;
    }

    @Override
    public void increaseStockFromOrderCanceled(IncreaseStockCommand command) {
        Long productId = command.productId();
        Product product = loadProductPort.loadProductWithLock(productId);
        product.increaseStockQuantity(command.quantity());
        persistProductPort.persist(product);
    }
}
