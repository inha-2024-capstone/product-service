package com.yoger.productserviceorganization.product.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.yoger.productserviceorganization.product.domain.model.PriceByQuantity;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.dto.response.SimpleSellableProductResponseDTO;
import com.yoger.productserviceorganization.product.persistence.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {
    @InjectMocks
    private ProductService productService; // 테스트할 대상

    @Mock
    private ProductRepository productRepository; // 목 객체

    private List<PriceByQuantity> priceByQuantities;

    @BeforeEach
    void setUp() {
        priceByQuantities = List.of(
                new PriceByQuantity(100, 10000),
                new PriceByQuantity(1000, 8500),
                new PriceByQuantity(10000, 7500)
        );
    }

    @Test
    void viewSellableProducts_ReturnsSellableProducts() {
        // Given
        Product sellableProductDomain = Product.of(
                null,
                "유효한 상품",
                priceByQuantities,
                "상품에 대한 설명입니다.",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                ProductState.SELLABLE,
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                1L, // creatorId
                "제작자 이름", // creatorName
                LocalDateTime.now().plusDays(30), // dueDate
                100, // initialStockQuantity
                20 // stockQuantity (80 판매됨)
        );
        given(productRepository.findByState(ProductState.SELLABLE)).willReturn(List.of(sellableProductDomain));

        // When
        List<SimpleSellableProductResponseDTO> result = productService.findSimpleSellableProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(sellableProductDomain.getId());
        assertThat(result.get(0).name()).isEqualTo("유효한 상품");
        assertThat(result.get(0).priceByQuantities()).isEqualTo(priceByQuantities);
        assertThat(result.get(0).state()).isEqualTo(ProductState.SELLABLE);
        assertThat(result.get(0).creatorName()).isEqualTo("제작자 이름");
        assertThat(result.get(0).dueDate()).isEqualTo(sellableProductDomain.getDueDate());
    }
}

