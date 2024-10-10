package com.yoger.productserviceorganization.product.dto.response;

import com.yoger.productserviceorganization.product.domain.model.PriceByQuantity;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import java.time.LocalDateTime;
import java.util.List;

public record SimpleSellableProductResponseDTO(
        Long id,
        String name,
        List<PriceByQuantity> priceByQuantities,
        String thumbnailImageUrl,
        String creatorName,
        int soldQuantity,
        LocalDateTime dueDate,
        ProductState state
) {
    public static SimpleSellableProductResponseDTO from(Product product) {
        int soldQuantity = product.getInitialStockQuantity() - product.getStockQuantity(); // 판매된 수량 계산
        return new SimpleSellableProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPriceByQuantities(),
                product.getThumbnailImageUrl(),
                product.getCreatorName(),
                soldQuantity,
                product.getDueDate(),
                product.getState()
        );
    }
}
