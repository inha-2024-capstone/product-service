package com.yoger.productserviceorganization.product.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class Product {
    private final Long id;
    private final String name;
    private final List<PriceByQuantity> priceByQuantities;
    private final String description;
    private final String imageUrl;
    private final ProductState state;
    private final String thumbnailImageUrl;
    private final Long creatorId;
    private final String creatorName;
    private final LocalDateTime dueDate;
    private final StockDomain stockDomain;

    private Product(
            Long id,
            String name,
            List<PriceByQuantity> priceByQuantities,
            String description,
            String imageUrl,
            ProductState state,
            String thumbnailImageUrl,
            Long creatorId,
            String creatorName,
            LocalDateTime dueDate,
            StockDomain stockDomain
    ) {
        this.id = id;
        this.name=name;
        this.priceByQuantities=priceByQuantities;
        this.description=description;
        this.imageUrl = imageUrl;
        this.state=state;
        this.thumbnailImageUrl=thumbnailImageUrl;
        this.creatorId=creatorId;
        this.creatorName=creatorName;
        this.dueDate=dueDate;
        this.stockDomain=stockDomain;
    }

    // 정적 팩토리 메서드
    public static Product of(
            Long id,
            String name,
            List<PriceByQuantity> priceByQuantities,
            String description,
            String imageUrl,
            ProductState state,
            String thumbnailImageUrl,
            Long creatorId,
            String creatorName,
            LocalDateTime dueDate,
            int initialStockQuantity,
            int stockQuantity
    ) {
        validate();
        StockDomain stockDomain = new StockDomain(initialStockQuantity, stockQuantity);
        return new Product(
                id,
                name,
                priceByQuantities,
                description,
                imageUrl,
                state,
                thumbnailImageUrl,
                creatorId,
                creatorName,
                dueDate,
                stockDomain
        );
    }

    private static void validate() {
        //TODO: 필드들에 대해 필요한 유효성 검증 로직 작성
    }

    public void decreaseStockQuantity(int amount) {
        this.stockDomain.decrease(amount);
    }

    public int getStockQuantity() {
        return stockDomain.getStockQuantity();
    }

    public int getInitialStockQuantity() {
        return stockDomain.getInitialStockQuantity();
    }
}

