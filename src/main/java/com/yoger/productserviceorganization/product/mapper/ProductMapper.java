package com.yoger.productserviceorganization.product.mapper;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.dto.request.DemoProductRequestDTO;
import com.yoger.productserviceorganization.product.persistence.ProductEntity;

public class ProductMapper {
    public static ProductEntity toEntityFrom(Product product) {
        return ProductEntity.of(
                product.getName(),
                product.getPriceByQuantities(),
                product.getDescription(),
                product.getImageUrl(),
                product.getState(),
                product.getThumbnailImageUrl(),
                product.getCreatorId(),
                product.getCreatorName(),
                product.getDueDate(),
                product.getInitialStockQuantity(),
                product.getStockQuantity()
        );
    }

    public static Product toDomainFrom(ProductEntity productEntity) {
        return Product.of(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getPriceByQuantities(),
                productEntity.getDescription(),
                productEntity.getImageUrl(),
                productEntity.getState(),
                productEntity.getThumbnailImageUrl(),
                productEntity.getCreatorId(),
                productEntity.getCreatorName(),
                productEntity.getDueDate(),
                productEntity.getInitialStockQuantity(),
                productEntity.getStockQuantity()
        );
    }

    public static Product toDomainFrom(
            DemoProductRequestDTO demoProductRequestDTO,
            String imageUrl,
            String thumbnailImageUrl
    ) {
        return Product.of(
                null, // ID는 아직 생성되지 않았으므로 null
                demoProductRequestDTO.name(),
                null,
                demoProductRequestDTO.description(),
                imageUrl,
                ProductState.DEMO,
                thumbnailImageUrl,
                demoProductRequestDTO.creatorId(),
                demoProductRequestDTO.creatorName(),
                null,
                0,
                0
        );
    }
}
