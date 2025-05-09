package com.yoger.productserviceorganization.product.adapters.web.controller;

import com.yoger.productserviceorganization.product.adapters.web.dto.response.ProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleProductResponseDTO;
import com.yoger.productserviceorganization.product.application.port.in.GetProductQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final GetProductQuery getProductQuery;

    @GetMapping
    public ResponseEntity<List<SimpleProductResponseDTO>> getSellableProducts() {
        List<SimpleProductResponseDTO> products = getProductQuery.getProducts().stream().map(
                SimpleProductResponseDTO::from
        ).toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> getSellableProduct(@PathVariable Long productId) {
        ProductResponseDTO sellableProductDTO = ProductResponseDTO.from(getProductQuery.getProduct(productId));
        return ResponseEntity.ok(sellableProductDTO);
    }
}
