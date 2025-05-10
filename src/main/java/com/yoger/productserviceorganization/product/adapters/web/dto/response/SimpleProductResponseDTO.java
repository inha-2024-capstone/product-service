package com.yoger.productserviceorganization.product.adapters.web.dto.response;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import java.time.LocalDateTime;

public record SimpleProductResponseDTO(
        Long id,
        String name,
        Integer price,
        String thumbnailImageUrl,
        String creatorName,
        LocalDateTime dueDate,
        ProductState state,
        Integer stockQuantity
) {
    public static SimpleProductResponseDTO from(Product product) {
        return new SimpleProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getThumbnailImageUrl(),
                product.getCreatorName(),
                product.getDueDate(),
                product.getState(),
                product.getStockQuantity()
        );
    }
}
