package com.yoger.productserviceorganization.product.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductUnitTest {
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.of(
                1L,
                "Test Product",
                1000,
                "Test Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                50
        );
    }

    @Test
    void testDecreaseStockQuantitySuccess() {
        product.deductStockQuantity(10);
        assertThat(product.getStockQuantity()).isEqualTo(40);
    }

    @Test
    void testDecreaseStockQuantityWithExceedingAmount() {
        assertThatThrownBy(() -> product.deductStockQuantity(60))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void testProductCreationWithValidSellableState() {
        Product product = Product.of(
                1L,
                "Valid Sellable Product",
                10000,
                "Valid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                50
        );
        assertThat(product).isNotNull();
    }
}

