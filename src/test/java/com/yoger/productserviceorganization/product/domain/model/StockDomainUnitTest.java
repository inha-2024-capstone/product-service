package com.yoger.productserviceorganization.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;
import org.junit.jupiter.api.Test;

public class StockDomainUnitTest {
    @Test
    void testStockDomainCreation() {
        StockDomain stockDomain = new StockDomain(100, 50);
        assertThat(stockDomain.getInitialStockQuantity()).isEqualTo(100);
        assertThat(stockDomain.getStockQuantity()).isEqualTo(50);
    }

    @Test
    void testStockDomainCreationWithInvalidValues() {
        assertThatThrownBy(() -> new StockDomain(-1, 50))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("초기 재고 수량은 0보다 작을 수 없습니다.");
    }

    @Test
    void testDecreaseStockQuantitySuccess() {
        StockDomain stockDomain = new StockDomain(100, 50);
        stockDomain.decrease(20);
        assertThat(stockDomain.getStockQuantity()).isEqualTo(30);
    }

    @Test
    void testDecreaseStockQuantityWithInvalidAmount() {
        StockDomain stockDomain = new StockDomain(100, 50);
        assertThatThrownBy(() -> stockDomain.decrease(0))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("감소할 수량은 0보다 커야 합니다.");
    }

    @Test
    void testDecreaseStockQuantityWithExceedingAmount() {
        StockDomain stockDomain = new StockDomain(100, 50);
        assertThatThrownBy(() -> stockDomain.decrease(60))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("재고 수량이 부족합니다.");
    }
}
