package com.yoger.productserviceorganization.product.application;


import com.yoger.productserviceorganization.product.adapters.persistence.jpa.OutboxEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommandFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommandsFromOrderEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.OutboxRepository;
import com.yoger.productserviceorganization.product.application.port.out.ProductRepository;
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
    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxEventFactory outboxEventFactory;

    private record DeductionResult(int totalDeduction, List<OutboxEvent> outboxEvents) {}

    @Override
    public void applyDeduction(DeductStockCommandsFromOrderEvent commands) {
        Product product = productRepository.findByIdWithLock(commands.productId());
        DeductionResult deductionResult = processDeductStockCommands(product, commands);

        outboxRepository.saveAll(deductionResult.outboxEvents);

        product.deductStockQuantity(deductionResult.totalDeduction);
        productRepository.save(product);
    }

    private DeductionResult processDeductStockCommands(
            Product product,
            DeductStockCommandsFromOrderEvent commands
    ) {
        int totalDeduction = 0;
        List<OutboxEvent> outboxEvents = new ArrayList<>();

        for (DeductStockCommandFromOrderEvent commandEvent : commands.deductStockCommands()) {
            int orderQuantity = commandEvent.deductStockCommand().quantity();
            if (product.canDeduct(totalDeduction, orderQuantity)) {
                totalDeduction += orderQuantity;
                outboxEvents.add(outboxEventFactory.createDeductionCompletedEvent(product.getId(), commandEvent));
            } else {
                outboxEvents.add(outboxEventFactory.createDeductionFailedEvent(product.getId(), commandEvent));
            }
        }
        return new DeductionResult(totalDeduction, outboxEvents);
    }
}
