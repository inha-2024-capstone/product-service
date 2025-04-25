package com.yoger.productserviceorganization.product.domain.model;

import com.yoger.productserviceorganization.product.domain.exception.InvalidProductException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidTimeSetException;
import com.yoger.productserviceorganization.product.domain.exception.ProductCreatorMismatchException;
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
    private final String thumbnailImageUrl;
    private final ProductState state;
    private final Long creatorId;
    private final String creatorName;
    private final LocalDateTime dueDate;
    private final Stock stock;

    private Product(
            Long id,
            String name,
            List<PriceByQuantity> priceByQuantities,
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
        this.priceByQuantities = priceByQuantities;
        this.description = description;
        this.imageUrl = imageUrl;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.state = state;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.dueDate = dueDate;
        this.stock = stock;
    }

    // 정적 팩토리 메서드
    public static Product of(
            Long id,
            String name,
            List<PriceByQuantity> priceByQuantities,
            String description,
            String imageUrl,
            String thumbnailImageUrl,
            ProductState state,
            Long creatorId,
            String creatorName,
            LocalDateTime dueDate,
            int initialStockQuantity,
            int stockQuantity
    ) {
        validateFieldsByState(state, priceByQuantities, dueDate);
        Stock stock = new Stock(initialStockQuantity, stockQuantity);
        stock.validateByState(state);
        return new Product(
                id,
                name,
                priceByQuantities,
                description,
                imageUrl,
                thumbnailImageUrl,
                state,
                creatorId,
                creatorName,
                dueDate,
                stock
        );
    }

    public static Product updatedDemoProduct(
            Product existingProduct,
            String updatedName,
            String updatedDescription,
            String updatedImageUrl,
            String updatedThumbnailImageUrl
    ) {
        return new Product(
                existingProduct.getId(),
                updatedName,
                existingProduct.getPriceByQuantities(),
                updatedDescription,
                updatedImageUrl,
                updatedThumbnailImageUrl,
                existingProduct.getState(),
                existingProduct.getCreatorId(),
                existingProduct.getCreatorName(),
                existingProduct.getDueDate(),
                existingProduct.getStock()
        );
    }

    private static void validateFieldsByState(ProductState state, List<PriceByQuantity> priceByQuantities,
                                              LocalDateTime dueDate) {
        switch (state) {
            case SELLABLE -> validateFieldsInSellableState(priceByQuantities, dueDate);
            case DEMO -> validateFieldsInDemoState(priceByQuantities, dueDate);
            case SALE_ENDED -> {
                // SALE_ENDED 상태에서는 별도의 검증 추후에 추가
            }
            default -> throw new InvalidProductException("잘못된 상태입니다.");
        }
    }

    private static void validateFieldsInSellableState(List<PriceByQuantity> priceByQuantities, LocalDateTime dueDate) {
        //판매 가능 상품 필드들에 대해 필요한 유효성 검증 로직 작성
        if (priceByQuantities == null || priceByQuantities.isEmpty()) {
            throw new InvalidProductException("판매 가능 상품에 가격 정보가 비어 있을 수 없습니다.");
        }
        if (dueDate == null) {
            throw new InvalidTimeSetException("판매 가능 상품은 기한이 필요합니다.");
        }
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new InvalidTimeSetException("상품의 기한은 과거일 수 없습니다.");
        }
    }

    private static void validateFieldsInDemoState(List<PriceByQuantity> priceByQuantities, LocalDateTime dueDate) {
        //데모 상품 필드들에 대해 필요한 유효성 검증 로직 작성
        if (priceByQuantities != null) {
            throw new InvalidProductException("데모 상품은 가격 정보를 가질 수 없습니다.");
        }
        if (dueDate != null) {
            throw new InvalidTimeSetException("데모 상품은 기한을 가질 수 없습니다.");
        }
    }

    public static Product toSellableFrom(Product product, List<PriceByQuantity> priceByQuantities, LocalDateTime dueDate) {
        product.validateUnexpectedState(ProductState.DEMO);
        return new Product(
                product.getId(),
                product.getName(),
                priceByQuantities,
                product.getDescription(),
                product.getImageUrl(),
                product.getThumbnailImageUrl(),
                ProductState.SELLABLE,
                product.getCreatorId(),
                product.getCreatorName(),
                dueDate,
                new Stock(100_000_000, 100_000_000)
        );
    }

    public static Product toSaleEndedFrom(Product sellableProduct, Integer soldAmount, Integer finalPrice) {
        sellableProduct.validateUnexpectedState(ProductState.SELLABLE);
        return new Product(
                sellableProduct.id,
                sellableProduct.name,
                List.of(new PriceByQuantity(soldAmount, finalPrice)),
                sellableProduct.description,
                sellableProduct.imageUrl,
                sellableProduct.thumbnailImageUrl,
                ProductState.SALE_ENDED,
                sellableProduct.creatorId,
                sellableProduct.creatorName,
                sellableProduct.dueDate,
                sellableProduct.stock
        );
    }

    public void deductStockQuantity(Integer quantity) {
        validateUnexpectedState(ProductState.SELLABLE);
        this.stock.change(-quantity);
    }

    public void increaseStockQuantity(Integer quantity) {
        validateUnexpectedState(ProductState.SELLABLE);
        this.stock.change(quantity);
    }

    public int getStockQuantity() {
        return stock.getStockQuantity();
    }

    public int getInitialStockQuantity() {
        return stock.getInitialStockQuantity();
    }

    public void validateUnexpectedState(ProductState expectedState) {
        if (isUnexpectedState(expectedState)) {
            throw new InvalidProductException("상품이 예상된 상태가 아닙니다.");
        }
    }

    private boolean isUnexpectedState(ProductState expectedState) {
        return !this.state.equals(expectedState);
    }

    public void validateCreatorId(Long creatorId) {
        if (isInvalidCreatorId(creatorId)) {
            throw new ProductCreatorMismatchException("상품의 생성자 ID가 일치하지 않습니다.");
        }
    }

    private boolean isInvalidCreatorId(Long creatorId) {
        return !this.creatorId.equals(creatorId);
    }

    public Boolean isDemo() {
        return this.state == ProductState.DEMO;
    }

    public boolean canDeduct(int totalOrderQuantity, int orderQuantity) {
        return totalOrderQuantity + orderQuantity <= getStockQuantity();
    }
}
