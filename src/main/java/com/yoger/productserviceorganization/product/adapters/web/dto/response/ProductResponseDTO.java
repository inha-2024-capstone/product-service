package com.yoger.productserviceorganization.product.adapters.web.dto.response;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import java.time.LocalDateTime;

public record ProductResponseDTO(
        Long id,
        String name,
        Integer price,
        String description,
        String imageUrl,
        ProductState state,
        Long creatorId,
        String creatorName,
        LocalDateTime dueDate,
        int soldQuantity
) {
    public static ProductResponseDTO from(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getState(),
                product.getCreatorId(),
                product.getCreatorName(),
                product.getDueDate(),
                product.getStockQuantity()
        );
    }
}
