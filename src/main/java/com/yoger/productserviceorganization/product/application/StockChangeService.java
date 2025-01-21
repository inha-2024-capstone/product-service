package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommandFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.IncreaseStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.OutboxRepository;
import com.yoger.productserviceorganization.product.application.port.out.ProductRepository;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommandsFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
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
    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxEventFactory outboxEventFactory;

    @Override
    public void deductStockFromOrderCreated(DeductStockCommandsFromOrderEvent commands) {
        Long productId = commands.productId();
        Product product = productRepository.findByIdWithLock(productId);
        int currentStockQuantity = product.getStockQuantity();
        int totalOrderQuantity = 0;
        List<OutboxEvent> outboxEvents = new ArrayList<>();
        for (DeductStockCommandFromOrderEvent deductStockCommandFromOrderEvent : commands.deductStockCommands()) {
            Integer orderQuantity = deductStockCommandFromOrderEvent.deductStockCommand().quantity();
            if (canDeduct(currentStockQuantity, totalOrderQuantity, orderQuantity)) {
                totalOrderQuantity += orderQuantity;
                outboxEvents.add(outboxEventFactory.createDeductionCompletedEvent(productId, deductStockCommandFromOrderEvent));
            } else {
                outboxEvents.add(outboxEventFactory.createDeductionFailedEvent(productId, deductStockCommandFromOrderEvent));
            }
        }
        outboxRepository.saveAll(outboxEvents);

        product.deductStockQuantity(totalOrderQuantity);
        productRepository.save(product);
    }

    private boolean canDeduct(int currentStockQuantity, int totalOrderQuantity, int orderQuantity) {
        return totalOrderQuantity + orderQuantity <= currentStockQuantity;
    }

    @Override
    public void increaseStockFromOrderCanceled(IncreaseStockCommand command) {
        Long productId = command.productId();
        Product product = productRepository.findByIdWithLock(productId);
        product.increaseStockQuantity(command.quantity());
        productRepository.save(product);
    }
}
