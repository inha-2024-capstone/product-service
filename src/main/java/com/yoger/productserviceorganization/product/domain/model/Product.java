package com.yoger.productserviceorganization.product.domain.model;

import com.yoger.productserviceorganization.product.domain.exception.InvalidProductException;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Product {
    private final Long id;
    private final String name;
    private final Integer price;
    private final String description;
    private final String imageUrl;
    private final String thumbnailImageUrl;
    private final ProductState state;
    private final Long creatorId;
    private final String creatorName;
    private final LocalDateTime dueDate;
    private final Stock StockQuantity;

    private Product(
            Long id,
            String name,
            Integer price,
            String description,
            String imageUrl,
            String thumbnailImageUrl,
            ProductState state,
            Long creatorId,
            String creatorName,
            LocalDateTime dueDate,
            Stock stock
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.state = state;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.dueDate = dueDate;
        this.StockQuantity = stock;
    }

    public static Product of(
            Long id,
            String name,
            Integer price,
            String description,
            String imageUrl,
            String thumbnailImageUrl,
            ProductState state,
            Long creatorId,
            String creatorName,
            LocalDateTime dueDate,
            int stockQuantity
    ) {
        return new Product(
                id,
                name,
                price,
                description,
                imageUrl,
                thumbnailImageUrl,
                state,
                creatorId,
                creatorName,
                dueDate,
                new Stock(stockQuantity)
        );
    }

    public static Product toSaleEndedFrom(Product sellableProduct) {
        sellableProduct.validateUnexpectedState(ProductState.SELLABLE);
        return new Product(
                sellableProduct.id,
                sellableProduct.name,
                sellableProduct.price,
                sellableProduct.description,
                sellableProduct.imageUrl,
                sellableProduct.thumbnailImageUrl,
                ProductState.SALE_ENDED,
                sellableProduct.creatorId,
                sellableProduct.creatorName,
                sellableProduct.dueDate,
                sellableProduct.StockQuantity
        );
    }

    public void deductStockQuantity(Integer quantity) {
        validateUnexpectedState(ProductState.SELLABLE);
        this.StockQuantity.change(-quantity);
    }

    public void increaseStockQuantity(Integer quantity) {
        validateUnexpectedState(ProductState.SELLABLE);
        this.StockQuantity.change(quantity);
    }

    public int getStockQuantity() {
        return StockQuantity.getStockQuantity();
    }

    public void validateUnexpectedState(ProductState expectedState) {
        if (isUnexpectedState(expectedState)) {
            throw new InvalidProductException("상품이 예상된 상태가 아닙니다.");
        }
    }

    private boolean isUnexpectedState(ProductState expectedState) {
        return !this.state.equals(expectedState);
    }
}
