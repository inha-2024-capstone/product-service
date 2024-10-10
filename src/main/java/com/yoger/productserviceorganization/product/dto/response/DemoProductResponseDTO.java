package com.yoger.productserviceorganization.product.dto.response;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;

public record DemoProductResponseDTO(
        Long id,
        String name,
        String description,
        String imageUrl,
        Long creatorId,
        String creatorName,
        ProductState state
        ) {
    public static DemoProductResponseDTO from(Product product) {
        return new DemoProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getCreatorId(),
                product.getCreatorName(),
                product.getState()
        );
    }
}
