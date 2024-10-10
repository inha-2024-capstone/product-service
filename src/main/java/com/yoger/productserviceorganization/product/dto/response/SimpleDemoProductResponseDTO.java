package com.yoger.productserviceorganization.product.dto.response;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;

public record SimpleDemoProductResponseDTO(
        Long id,
        String name,
        String thumbnailImageUrl,
        String creatorName,
        ProductState state
) {
    public static SimpleDemoProductResponseDTO from(Product product) {
        return new SimpleDemoProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getThumbnailImageUrl(),
                product.getCreatorName(),
                product.getState()
        );
    }
}
