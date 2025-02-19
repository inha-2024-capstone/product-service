package com.yoger.productserviceorganization.product.adapters.web.dto.response;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;

public record SimpleSaleEndedProductResponseDTO(
        Long id,
        String name,
        String thumbnailImageUrl,
        String creatorName,
        ProductState state
) {
    public static SimpleSaleEndedProductResponseDTO from(Product product) {
        return new SimpleSaleEndedProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getThumbnailImageUrl(),
                product.getCreatorName(),
                product.getState()
        );
    }
}
