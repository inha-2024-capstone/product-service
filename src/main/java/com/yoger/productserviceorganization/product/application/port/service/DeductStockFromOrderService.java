package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.DeductStockFromOrderUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand;
import com.yoger.productserviceorganization.product.application.port.in.command.DeductStockFromOrderCommand.OrderItem;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.application.port.out.SaveOutboxEventPort;
import com.yoger.productserviceorganization.product.domain.model.Product;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeductStockFromOrderService implements DeductStockFromOrderUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PersistProductPort persistProductPort;
    private final OutboxEventFactory outboxEventFactory;

    @Override
    public void deduct(final DeductStockFromOrderCommand command, final String tracingProps) {
        final List<OrderItem> sortedItems = sortItemsByProductId(command.getItems());

        final Map<Long, Product> productsById = findAndLockProducts(sortedItems);

        if (hasSufficientStockForAllItems(productsById, sortedItems)) {
            processSuccessfulDeduction(command, tracingProps, productsById, sortedItems);
        } else {
            publishOrderDeductionFailedEvent(command, tracingProps);
        }
    }

    private List<OrderItem> sortItemsByProductId(final List<OrderItem> items) {
        return items.stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .toList();
    }

    private Map<Long, Product> findAndLockProducts(final List<OrderItem> sortedItems) {
        final List<Long> productIds = sortedItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();

        return loadProductPort.loadProductsWithLock(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private boolean hasSufficientStockForAllItems(
            final Map<Long, Product> productsById,
            final List<OrderItem> items
    ) {
        return items.stream()
                .allMatch(item -> {
                    final Product product = productsById.get(item.getProductId());
                    return product.getStockQuantity() >= item.getQuantity();
                });
    }

    private void processSuccessfulDeduction(
            final DeductStockFromOrderCommand command,
            final String tracingProps,
            final Map<Long, Product> productsById,
            final List<OrderItem> items
    ) {
        deductStockQuantities(productsById, items);
        persistAllProducts(productsById);
        publishOrderDeductionSucceededEvent(command, tracingProps);
    }

    private void deductStockQuantities(
            final Map<Long, Product> productsById,
            final List<OrderItem> items
    ) {
        items.forEach(item -> {
            final Product product = productsById.get(item.getProductId());
            product.deductStockQuantity(item.getQuantity());
        });
    }

    private void persistAllProducts(final Map<Long, Product> productsById) {
        productsById.values().forEach(persistProductPort::persist);
    }

    private void publishOrderDeductionSucceededEvent(final DeductStockFromOrderCommand command, final String tracingProps) {
        saveOutboxEventPort.save(
                outboxEventFactory.createOrderDeductionSucceededEvent(command, tracingProps)
        );
    }

    private void publishOrderDeductionFailedEvent(final DeductStockFromOrderCommand command, final String tracingProps) {
        saveOutboxEventPort.save(
                outboxEventFactory.createOrderDeductionFailedEvent(command, tracingProps)
        );
    }
}
