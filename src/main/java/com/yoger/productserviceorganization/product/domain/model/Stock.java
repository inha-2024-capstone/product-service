package com.yoger.productserviceorganization.product.domain.model;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;
import lombok.AccessLevel;
import lombok.Getter;

//package-private 접근 제어로 패키지 밖에서는 이 StockDomain 클래스에 접근 불가
@Getter(AccessLevel.PACKAGE)
class Stock {
    private int stockQuantity;

    Stock(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new InvalidStockException("현재 재고 수량은 0보다 작을 수 없습니다.");
        }
        this.stockQuantity = stockQuantity;
    }

    void change(int amount) {
        if (this.stockQuantity + amount < 0) {
            throw new InsufficientStockException("재고 수량이 부족합니다.");
        }
        this.stockQuantity += amount;
    }
}
