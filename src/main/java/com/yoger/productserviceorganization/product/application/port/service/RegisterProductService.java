package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.in.RegisterProductUseCase;
import com.yoger.productserviceorganization.product.application.port.in.command.RegisterProductCommand;
import com.yoger.productserviceorganization.product.application.port.out.ManageProductImagePort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterProductService implements RegisterProductUseCase {
    private final PersistProductPort persistProductPort;
    private final ManageProductImagePort manageProductImagePort;

    @Override
    public Product register(RegisterProductCommand registerProductCommand) {
        String imageUrl = manageProductImagePort.uploadImage(registerProductCommand.getImage());
        String thumbnailImageUrl = manageProductImagePort.uploadImage(registerProductCommand.getThumbnailImage());

        registerTransactionSynchronizationForImageDeletion(imageUrl, thumbnailImageUrl);

        Product product = toDomainFrom(registerProductCommand, imageUrl, thumbnailImageUrl);
        return persistProductPort.persist(product);
    }

    private void registerTransactionSynchronizationForImageDeletion(String imageUrl, String thumbnailImageUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    deleteUploadedImages(imageUrl, thumbnailImageUrl);
                }
            }
        });
    }

    private void deleteUploadedImages(String... imageUrls) {
        for (String imageUrl : imageUrls) {
            manageProductImagePort.deleteImage(imageUrl);
        }
    }

    private Product toDomainFrom(
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
