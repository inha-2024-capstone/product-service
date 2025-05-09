package com.yoger.productserviceorganization.product.mapper;

import com.yoger.productserviceorganization.product.application.port.in.command.RegisterProductCommand;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.adapters.persistence.jpa.ProductJpaEntity;

public class ProductMapper {
    private ProductMapper() {}

    public static ProductJpaEntity toEntityFrom(Product product) {
        return ProductJpaEntity.of(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                product.getThumbnailImageUrl(),
                product.getState(),
                product.getCreatorId(),
                product.getCreatorName(),
                product.getDueDate(),
                product.getStockQuantity()
        );
    }

    public static Product toDomainFrom(ProductJpaEntity productEntity) {
        return Product.of(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getPrice(),
                productEntity.getDescription(),
                productEntity.getImageUrl(),
                productEntity.getThumbnailImageUrl(),
                productEntity.getState(),
                productEntity.getCreatorId(),
                productEntity.getCreatorName(),
                productEntity.getDueDate(),
                productEntity.getStockQuantity()
        );
    }

    public static Product toDomainFrom(
            RegisterProductCommand registerProductCommand,
            String imageUrl,
            String thumbnailImageUrl
    ) {
        return Product.of(
                null, // ID는 아직 생성되지 않았으므로 null
                registerProductCommand.getName(),
                registerProductCommand.getPrice(),
                registerProductCommand.getDescription(),
                imageUrl,
                thumbnailImageUrl,
                ProductState.SELLABLE,
                registerProductCommand.getCreatorId(),
                registerProductCommand.getCreatorName(),
                registerProductCommand.getDueDate(),
                registerProductCommand.getStockQuantity()
                );
    }
}
