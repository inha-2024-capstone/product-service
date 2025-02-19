package com.yoger.productserviceorganization.review.adapter.web.dto.request;

import com.yoger.productserviceorganization.review.adapter.web.dto.validation.ValidImages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ReviewSaveRequestDTO(
        @NotNull
        Integer starRating,

        @NotBlank
        String content,

        @ValidImages
        List<MultipartFile> images
) {
}
