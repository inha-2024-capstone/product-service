package com.yoger.productserviceorganization.product.adapters.web.controller;

import com.yoger.productserviceorganization.product.application.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/products/common")
@RequiredArgsConstructor
public class CommonProductController {
    private final ProductService productService;

    @GetMapping("/creator")
    public ResponseEntity<List<?>> getSimpleProductInfosByCreator(
            @RequestHeader(value = "User-Id") Long creatorId
    ) {
        List<?> demoProducts = productService.findSimpleProductsByCreatorId(creatorId);
        return ResponseEntity.ok(demoProducts);
    }
}
