package com.yoger.productserviceorganization.product.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductUnitTest {
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.of(
                1L,
                "Test Product",
                List.of(new PriceByQuantity(1, 100)),
                "Test Description",
                "http://image.url",
                ProductState.SELLABLE,
                "http://thumbnail.url",
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                100,
                50
        );
    }

    @Test
    void testDecreaseStockQuantitySuccess() {
        product.decreaseStockQuantity(10);
        assertThat(product.getStockDomain().getStockQuantity()).isEqualTo(40);
    }

    @Test
    void testDecreaseStockQuantityWithInvalidAmount() {
        assertThatThrownBy(() -> product.decreaseStockQuantity(0))
                .isInstanceOf(InvalidStockException.class)
                .hasMessage("감소할 수량은 0보다 커야 합니다.");
    }

    @Test
    void testDecreaseStockQuantityWithExceedingAmount() {
        assertThatThrownBy(() -> product.decreaseStockQuantity(60))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("재고 수량이 부족합니다.");
    }
}

