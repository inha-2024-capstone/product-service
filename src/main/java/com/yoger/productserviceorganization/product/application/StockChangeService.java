package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.EventProducer;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionCompletedEvent;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event.DeductionFailedEvent;
import com.yoger.productserviceorganization.product.application.port.out.ProductRepository;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommand;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockCommands;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockOnOrderCreatedUseCase;
import com.yoger.productserviceorganization.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class StockChangeService implements DeductStockOnOrderCreatedUseCase {
    private final ProductRepository productRepository;
    private final EventProducer eventProducer;

    @Override
    public void deductStock(DeductStockCommands deductStockCommands) {
        Long productId = deductStockCommands.productId();
        Product product = productRepository.findById(productId);
        int currentStockQuantity = product.getStockQuantity();
        int totalOrderQuantity = 0;
        for(DeductStockCommand deductStockCommand : deductStockCommands.deductStockCommands()) {
            // 이미 처리된 이벤트인가 검증로직이 추가되어야 함.
            if (totalOrderQuantity + deductStockCommand.orderQuantity() <= currentStockQuantity) {
                totalOrderQuantity += deductStockCommand.orderQuantity();
                eventProducer.sendDeductionCompletedEvent(
                        DeductionCompletedEvent.from(productId, deductStockCommand)
                );
            } else {
                eventProducer.sendDeductionFailedEvent(
                        DeductionFailedEvent.from(productId, deductStockCommand)
                );
            }
        }
        product.changeStockQuantity(-totalOrderQuantity);
        productRepository.save(product);
    }
}
