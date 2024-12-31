package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.adapters.web.dto.request.DemoProductRequestDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.request.UpdatedDemoProductRequestDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.DemoProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleSaleEndedProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.partialRefundRequestDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SellableProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleDemoProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleSellableProductResponseDTO;
import com.yoger.productserviceorganization.product.domain.exception.InvalidProductException;
import com.yoger.productserviceorganization.product.domain.exception.InvalidStockException;
import com.yoger.productserviceorganization.product.domain.model.PriceByQuantity;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.application.port.ProductImageStorage;
import com.yoger.productserviceorganization.product.application.port.ProductRepository;
import com.yoger.productserviceorganization.product.mapper.ProductMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductImageStorage productImageStorage;

    public List<SimpleSellableProductResponseDTO> findSimpleSellableProducts() {
        return productRepository.findByState(ProductState.SELLABLE).stream()
                .map(this::mapToSellableDTO)
                .toList();
    }

    private SimpleSellableProductResponseDTO mapToSellableDTO(Product product) {
        return SimpleSellableProductResponseDTO.from(product);
    }

    @Transactional
    public DemoProductResponseDTO saveDemoProduct(Long creatorId, @Valid DemoProductRequestDTO demoProductRequestDTO) {
        String imageUrl = productImageStorage.uploadImage(demoProductRequestDTO.image());
        String thumbnailImageUrl = productImageStorage.uploadImage(demoProductRequestDTO.thumbnailImage());

        registerTransactionSynchronizationForImageDeletion(imageUrl, thumbnailImageUrl);

        Product product = ProductMapper.toDomainFrom(creatorId, demoProductRequestDTO, imageUrl, thumbnailImageUrl);
        return DemoProductResponseDTO.from(productRepository.save(product));
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
            productImageStorage.deleteImage(imageUrl);
        }
    }

    public List<SimpleDemoProductResponseDTO> findSimpleDemoProducts() {
        return productRepository.findByState(ProductState.DEMO).stream()
                .map(this::mapToDemoDTO)
                .toList();
    }

    private SimpleDemoProductResponseDTO mapToDemoDTO(Product product) {
        return SimpleDemoProductResponseDTO.from(product);
    }

    public SellableProductResponseDTO findSellableProduct(Long productId) {
        Product product = productRepository.findById(productId);
        product.validateUnexpectedState(ProductState.SELLABLE);
        return SellableProductResponseDTO.from(product);
    }

    public Boolean isDemoProduct(Long productId) {
        return productRepository.findById(productId).isDemo();
    }

    public DemoProductResponseDTO findDemoProduct(Long productId) {
        Product product = productRepository.findById(productId);
        product.validateUnexpectedState(ProductState.DEMO);
        return DemoProductResponseDTO.from(product);
    }

    @Transactional
    public DemoProductResponseDTO updateDemoProduct(
            Long productId,
            Long creatorId,
            UpdatedDemoProductRequestDTO updatedDemoProductRequestDTO
    ) {
        Product product = productRepository.findByIdWithLock(productId);
        product.validateUnexpectedState(ProductState.DEMO);
        product.validateCreatorId(creatorId);

        String updatedProductName = product.getName();
        if (updatedDemoProductRequestDTO.name() != null) {
            updatedProductName = updatedDemoProductRequestDTO.name();
        }
        String updatedProductDescription = product.getDescription();
        if (updatedDemoProductRequestDTO.description() != null) {
            updatedProductDescription = updatedDemoProductRequestDTO.description();
        }
        String updatedImageUrl = productImageStorage.updateImage(
                updatedDemoProductRequestDTO.image(),
                product.getImageUrl()
        );
        String updatedThumbnailImageUrl = productImageStorage.updateImage(
                updatedDemoProductRequestDTO.thumbnailImage(),
                product.getThumbnailImageUrl()
        );
        Product updatedProduct = Product.updatedDemoProduct(
                product,
                updatedProductName,
                updatedProductDescription,
                updatedImageUrl,
                updatedThumbnailImageUrl
        );

        Product savedProduct = productRepository.save(updatedProduct);
        return DemoProductResponseDTO.from(savedProduct);
    }

    @Transactional
    public void updateDemoToSellable(Long productId, Long creatorId, List<PriceByQuantity> priceByQuantities, LocalDateTime dueDate) {
        Product demoProduct = productRepository.findById(productId);
        demoProduct.validateCreatorId(creatorId);

        Product sellableProduct = Product.toSellableFrom(demoProduct, priceByQuantities, dueDate);
        productRepository.updateForState(sellableProduct, ProductState.DEMO);
    }

    @Override
    @Transactional
    public void deleteDemoProduct(Long productId, Long creatorId) {
        deleteProductWithState(productId, creatorId, ProductState.DEMO);
    }

    private void deleteProductWithState(Long productId, Long creatorId, ProductState state) {
        if(state.equals(ProductState.SELLABLE)) {
            throw new InvalidProductException("판매중인 상품은 삭제할 수 없습니다.");
        }
        Product product = productRepository.findByIdWithLock(productId);
        product.validateUnexpectedState(state);
        product.validateCreatorId(creatorId);

        productRepository.deleteById(productId);
        deleteUploadedImages(product.getImageUrl(), product.getThumbnailImageUrl());
    }

    @Transactional
    public void changeSellableProductStock(Long productId, Integer quantity) {
        int flag = productRepository.updateStock(productId, quantity);
        if (flag == 0) {
            throw new InvalidStockException("상품이 판매가능 하지 않거나, 상품의 재고가 부족합니다.");
        }
    }

    @Override
    public List<?> findSimpleProductsByCreatorId(Long creatorId) {
        return productRepository.findByCreatorId(creatorId)
                .stream()
                .map(product -> {
                    if (product.getState().equals(ProductState.SELLABLE)) {
                        return SimpleSellableProductResponseDTO.from(product);
                    } else if(product.getState().equals(ProductState.DEMO)) {
                        return SimpleDemoProductResponseDTO.from(product);
                    } else {
                        return SimpleSaleEndedProductResponseDTO.from(product);
                    }
                })
                .toList();
    }

    @Override
    @Transactional
    public partialRefundRequestDTO updateSellableToSaleEnded(Long productId, Integer soldAmount) {
        Product product = productRepository.findById(productId);
        int originPrice = product.getPriceByQuantities().getFirst().price();
        int finalPrice = originPrice;
        for(PriceByQuantity priceByQuantity : product.getPriceByQuantities()) {
            if(priceByQuantity.isLargerThenSoldAmount(soldAmount)) {
                finalPrice = priceByQuantity.price();
            }
        }
        Product saleEndedProduct = Product.toSaleEndedFrom(product, soldAmount, finalPrice);
        productRepository.updateForState(saleEndedProduct, ProductState.SELLABLE);
        return new partialRefundRequestDTO(productId, originPrice, finalPrice);
    }
}
