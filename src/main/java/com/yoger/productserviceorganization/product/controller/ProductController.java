package com.yoger.productserviceorganization.product.controller;

import com.yoger.productserviceorganization.product.domain.ProductService;
import com.yoger.productserviceorganization.product.dto.request.DemoProductRequestDTO;
import com.yoger.productserviceorganization.product.dto.response.DemoProductResponseDTO;
import com.yoger.productserviceorganization.product.dto.response.SimpleDemoProductResponseDTO;
import com.yoger.productserviceorganization.product.dto.response.SimpleSellableProductResponseDTO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<SimpleSellableProductResponseDTO>> getSellableProducts() {
        List<SimpleSellableProductResponseDTO> products = productService.findSimpleSellableProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<DemoProductResponseDTO> saveDemo(@Valid @ModelAttribute DemoProductRequestDTO demoProductRequestDTO) {
        DemoProductResponseDTO savedProduct = productService.saveDemoProduct(demoProductRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping("/demo")
    public ResponseEntity<List<SimpleDemoProductResponseDTO>> getDemoProducts() {
        List<SimpleDemoProductResponseDTO> demoProducts = productService.findSimpleDemoProducts();
        return ResponseEntity.ok(demoProducts);
    }
}

