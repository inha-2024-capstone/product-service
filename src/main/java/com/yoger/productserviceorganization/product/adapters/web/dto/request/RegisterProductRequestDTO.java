package com.yoger.productserviceorganization.product.adapters.web.dto.request;

import com.yoger.productserviceorganization.product.application.port.in.command.RegisterProductCommand;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

public record RegisterProductRequestDTO(
        String name,
        Integer price,
        String description,
        MultipartFile image,
        MultipartFile thumbnailImage,
        String creatorName,
        LocalDateTime dueDate,
        Integer stockQuantity
) {
    public RegisterProductCommand toCommand(Long creatorId) {
        return new RegisterProductCommand(
                name,
                price,
                description,
                image,
                thumbnailImage,
                creatorId,
                creatorName,
                dueDate,
                stockQuantity
        );
    }
}
