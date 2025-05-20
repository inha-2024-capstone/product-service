package com.yoger.productserviceorganization.product.adapters.web.controller;

import com.yoger.productserviceorganization.product.adapters.web.dto.request.RegisterProductRequestDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.ProductResponseDTO;
import com.yoger.productserviceorganization.product.application.port.in.RegisterProductUseCase;
import com.yoger.productserviceorganization.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/register")
@RequiredArgsConstructor
class RegisterProductController {
    private final RegisterProductUseCase registerProductUseCase;

    //TODO : 추후 배포시에는 Interceptor 를 사용하여, ADMIN 권한을 가진 사용자만 register 하도록 방어
    @PostMapping
    public ResponseEntity<ProductResponseDTO> registerProduct(
            @RequestHeader(value = "User-Id") Long creatorId,
            @ModelAttribute RegisterProductRequestDTO registerProductRequestDTO
    ) {
        Product product = registerProductUseCase.register(registerProductRequestDTO.toCommand(creatorId));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ProductResponseDTO.from(product)
        );
    }
}
