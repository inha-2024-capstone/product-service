package com.yoger.productserviceorganization.product.adapters.persistence.jpa;

import com.yoger.productserviceorganization.product.domain.model.Product;

final class ProductMapper {
    private ProductMapper() {}

    static ProductJpaEntity toEntityFrom(Product product) {
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

    static Product toDomainFrom(ProductJpaEntity productEntity) {
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
}
