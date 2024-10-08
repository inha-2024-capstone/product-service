package com.yoger.productserviceorganization.proruct.dto.response;

import com.yoger.productserviceorganization.proruct.domain.model.PriceByQuantity;
import com.yoger.productserviceorganization.proruct.domain.model.ProductState;
import com.yoger.productserviceorganization.proruct.persistence.ProductEntity;
import java.util.List;

public record SellableProductResponseDTO(
        Long id,
        String name,
        List<PriceByQuantity> priceByQuantities,
        String description,
        String imageUrl,
        ProductState state
) {
    public static SellableProductResponseDTO from(ProductEntity productEntity) {
        return new SellableProductResponseDTO(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getPriceByQuantities(),
                productEntity.getDescription(),
                productEntity.getImageUrl(),
                productEntity.getState()
        );
    }
}
