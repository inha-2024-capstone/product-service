package com.yoger.productserviceorganization.product.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.yoger.productserviceorganization.product.domain.exception.InsufficientStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidProductException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidTimeSetException;
import java.time.LocalDate;
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
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                100,
                50
        );
    }

    @Test
    void testUpdateToSellableStateSuccess() {
        Product demoProduct = Product.of(
                1L,
                "Test Product",
                null,
                "Test Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.DEMO,
                101L,
                "Creator Name",
                null,
                0,
                0
        );

        Product sellableProduct = Product.toSellableFrom(demoProduct, List.of(new PriceByQuantity(1, 100)),
                LocalDate.MAX.atStartOfDay());

        assertThat(demoProduct)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ProductState.class)
                .ignoringFields("priceByQuantities", "stock.initialStockQuantity", "stock.stockQuantity", "dueDate")
                .isEqualTo(sellableProduct);
    }

    @Test
    void testUpdateToSellableStateFailure() {
        Product sellableProduct = Product.of(
                1L,
                "Test Product",
                List.of(new PriceByQuantity(1, 100)),
                "Test Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDate.MAX.atStartOfDay(),
                3,
                3
        );

        assertThatThrownBy(() -> Product.toSellableFrom(sellableProduct, List.of(new PriceByQuantity(1, 100)),
                LocalDate.MAX.atStartOfDay()))
                .isInstanceOf(InvalidProductException.class);
    }


    @Test
    void testDecreaseStockQuantitySuccess() {
        product.deductStockQuantity(10);
        assertThat(product.getStock().getStockQuantity()).isEqualTo(40);
    }

    @Test
    void testDecreaseStockQuantityWithInvalidAmount() {
        assertThatThrownBy(() -> product.deductStockQuantity(0))
                .isInstanceOf(InvalidStockException.class);
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
                List.of(new PriceByQuantity(1, 100)),
                "Valid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                100,
                50
        );
        assertThat(product).isNotNull();
    }

    @Test
    void testProductCreationWithSellableStateAndNullPriceByQuantities() {
        assertThatThrownBy(() -> Product.of(
                1L,
                "Invalid Sellable Product",
                null,
                "Invalid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                100,
                50
        )).isInstanceOf(InvalidProductException.class)
                .hasMessage("판매 가능 상품에 가격 정보가 비어 있을 수 없습니다.");
    }

    @Test
    void testProductCreationWithSellableStateAndEmptyPriceByQuantities() {
        assertThatThrownBy(() -> Product.of(
                1L,
                "Invalid Sellable Product",
                List.of(),
                "Invalid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                100,
                50
        )).isInstanceOf(InvalidProductException.class)
                .hasMessage("판매 가능 상품에 가격 정보가 비어 있을 수 없습니다.");
    }

    @Test
    void testProductCreationWithSellableStateAndNullDueDate() {
        assertThatThrownBy(() -> Product.of(
                1L,
                "Invalid Sellable Product",
                List.of(new PriceByQuantity(1, 100)),
                "Invalid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                null,
                100,
                50
        )).isInstanceOf(InvalidTimeSetException.class)
                .hasMessage("판매 가능 상품은 기한이 필요합니다.");
    }

    @Test
    void testProductCreationWithSellableStateAndPastDueDate() {
        assertThatThrownBy(() -> Product.of(
                1L,
                "Invalid Sellable Product",
                List.of(new PriceByQuantity(1, 100)),
                "Invalid Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.SELLABLE,
                101L,
                "Creator Name",
                LocalDateTime.now().minusDays(1),
                100,
                50
        )).isInstanceOf(InvalidTimeSetException.class)
                .hasMessage("상품의 기한은 과거일 수 없습니다.");
    }

    @Test
    void testProductCreationWithValidDemoState() {
        Product product = Product.of(
                2L,
                "Valid Demo Product",
                null,
                "Demo Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.DEMO,
                102L,
                "Creator Name",
                null,
                0,
                0
        );
        assertThat(product).isNotNull();
    }

    @Test
    void testProductCreationWithDemoStateAndNonNullPriceByQuantities() {
        assertThatThrownBy(() -> Product.of(
                2L,
                "Invalid Demo Product",
                List.of(new PriceByQuantity(1, 100)),
                "Demo Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.DEMO,
                102L,
                "Creator Name",
                null,
                0,
                0
        )).isInstanceOf(InvalidProductException.class)
                .hasMessage("데모 상품은 가격 정보를 가질 수 없습니다.");
    }

    @Test
    void testProductCreationWithDemoStateAndNonNullDueDate() {
        assertThatThrownBy(() -> Product.of(
                2L,
                "Invalid Demo Product",
                null,
                "Demo Description",
                "http://image.url",
                "http://thumbnail.url",
                ProductState.DEMO,
                102L,
                "Creator Name",
                LocalDateTime.now().plusDays(30),
                0,
                0
        )).isInstanceOf(InvalidTimeSetException.class)
                .hasMessage("데모 상품은 기한을 가질 수 없습니다.");
    }

    @Test
    void testValidateUnexpectedStateWithDifferentState() {
        assertThatThrownBy(() -> product.validateUnexpectedState(ProductState.DEMO))
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("상품이 예상된 상태가 아닙니다.");
    }
}

