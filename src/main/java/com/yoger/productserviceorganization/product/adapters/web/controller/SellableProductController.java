package com.yoger.productserviceorganization.product.adapters.web.controller;

import com.yoger.productserviceorganization.product.adapters.web.dto.response.SellableProductResponseDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.SimpleSellableProductResponseDTO;
import com.yoger.productserviceorganization.product.application.ProductService;
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
public class SellableProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<SimpleSellableProductResponseDTO>> getSellableProducts() {
        List<SimpleSellableProductResponseDTO> products = productService.findSimpleSellableProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<SellableProductResponseDTO> getSellableProduct(@PathVariable Long productId) {
        SellableProductResponseDTO sellableProductDTO = productService.findSellableProduct(productId);
        return ResponseEntity.ok(sellableProductDTO);
    }
}
