package com.yoger.productserviceorganization.product.application.port.in.command;

import com.yoger.productserviceorganization.global.validator.SelfValidating;
import com.yoger.productserviceorganization.product.adapters.web.dto.validation.ValidImage;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegisterProductCommand extends SelfValidating<RegisterProductCommand> {

        @NotBlank(message = "상품 이름을 작성해주세요.")
        @Size(min = 2, max = 50, message = "상품 이름은 2글자 이상 50글자 이하만 가능합니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9\\-\\_ ]+$",
                message = "상품 이름은 한글, 영어, 숫자, '-', '_' 만 사용할 수 있습니다."
        )
        String name;

        @NotNull(message = "가격은 필수 항목입니다.")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        Integer price;

        @NotBlank(message = "상품에 대한 설명을 적어주세요.")
        @Size(min = 10, max = 500, message = "상품 상세 설명은 10글자 이상 500글자 이하만 가능합니다.")
        String description;

        @ValidImage
        MultipartFile image;

        @ValidImage
        MultipartFile thumbnailImage;

        @NotNull
        Long creatorId;

        @NotBlank
        String creatorName;

        @NotNull(message = "마감일은 필수입니다.")
        @Future(message = "마감일은 현재 시각보다 이후여야 합니다.")
        LocalDateTime dueDate;

        @NotNull(message = "초기 재고는 필수 항목입니다.")
        @Min(value = 0, message = "초기 재고는 0 이상이어야 합니다.")
        Integer stockQuantity;

        public RegisterProductCommand(
                String name,
                Integer price,
                String description,
                MultipartFile image,
                MultipartFile thumbnailImage,
                Long creatorId,
                String creatorName,
                LocalDateTime dueDate,
                Integer stockQuantity
        ) {
                this.name = name;
                this.price = price;
                this.description = description;
                this.image = image;
                this.thumbnailImage = thumbnailImage;
                this.creatorId = creatorId;
                this.creatorName = creatorName;
                this.dueDate = dueDate;
                this.stockQuantity = stockQuantity;

                this.validateSelf();
        }
}
