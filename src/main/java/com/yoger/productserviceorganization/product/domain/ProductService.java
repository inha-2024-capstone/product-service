package com.yoger.productserviceorganization.product.domain;

import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.dto.request.DemoProductRequestDTO;
import com.yoger.productserviceorganization.product.dto.response.DemoProductResponseDTO;
import com.yoger.productserviceorganization.product.dto.response.SimpleDemoProductResponseDTO;
import com.yoger.productserviceorganization.product.dto.response.SimpleSellableProductResponseDTO;
import com.yoger.productserviceorganization.product.mapper.ProductMapper;
import com.yoger.productserviceorganization.product.persistence.ProductRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    public List<SimpleSellableProductResponseDTO> findSimpleSellableProducts() {
        return productRepository.findByState(ProductState.SELLABLE).stream()
                .map(this::mapToSellableDTO)
                .toList();
    }

    private SimpleSellableProductResponseDTO mapToSellableDTO(Product product) {
        return SimpleSellableProductResponseDTO.from(product);
    }

    @Transactional
    public DemoProductResponseDTO saveDemoProduct(@Valid DemoProductRequestDTO demoProductRequestDTO) {
        String imageUrl = s3Service.uploadImage(demoProductRequestDTO.image());
        String thumbnailImageUrl = s3Service.uploadImage(demoProductRequestDTO.thumbnailImage());
        Product product = ProductMapper.toDomainFrom(demoProductRequestDTO, imageUrl, thumbnailImageUrl);
        return DemoProductResponseDTO.from(productRepository.save(product));
    }

    public List<SimpleDemoProductResponseDTO> findSimpleDemoProducts() {
        return productRepository.findByState(ProductState.DEMO).stream()
                .map(this::mapToDemoDTO)
                .toList();
    }

    private SimpleDemoProductResponseDTO mapToDemoDTO(Product product) {
        return SimpleDemoProductResponseDTO.from(product);
    }
}
