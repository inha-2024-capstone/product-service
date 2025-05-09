package com.yoger.productserviceorganization.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

public class StockUnitTest {
    @Test
    void testStockDomainCreation() {
        Stock stock = new Stock(50);
        assertThat(stock.getStockQuantity()).isEqualTo(50);
    }

    @Test
    void testDecreaseStockQuantitySuccess() {
        Stock stock = new Stock(50);
        stock.change(-20);
        assertThat(stock.getStockQuantity()).isEqualTo(30);
    }


    @Test
    void testDecreaseStockQuantityWithExceedingAmount() {
        Stock stock = new Stock(50);
        assertThatThrownBy(() -> stock.change(-60))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("재고 수량이 부족합니다.");
    }
}
