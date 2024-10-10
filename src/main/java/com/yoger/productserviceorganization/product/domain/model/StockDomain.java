package com.yoger.productserviceorganization.product.domain.model;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;

public class StockDomain {
    private final int initialStockQuantity;
    private int stockQuantity;

    StockDomain(int initialStockQuantity, int stockQuantity) {
        if (initialStockQuantity < 0) {
            throw new InvalidStockException("초기 재고 수량은 0보다 작을 수 없습니다.");
        }
        if (stockQuantity < 0) {
            throw new InvalidStockException("현재 재고 수량은 0보다 작을 수 없습니다.");
        }
        if (initialStockQuantity < stockQuantity) {
            throw new InvalidStockException("현재 재고가 초기 재고 수량보다 많을 수 없습니다.");
        }
        this.initialStockQuantity = initialStockQuantity;
        this.stockQuantity = stockQuantity;
    }

    void decrease(int amount) {
        if (amount <= 0) {
            throw new InvalidStockException("감소할 수량은 0보다 커야 합니다.");
        }
        if (this.stockQuantity < amount) {
            throw new InsufficientStockException("재고 수량이 부족합니다.");
        }
        this.stockQuantity-=amount;
    }

    int getStockQuantity() {
        return this.stockQuantity;
    }

    int getInitialStockQuantity() {
        return this.initialStockQuantity;
    }
}
