package com.yoger.productserviceorganization.product.application.port.in.command;

import com.yoger.productserviceorganization.global.validator.SelfValidating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class DeductStockCommand extends SelfValidating<DeductStockCommand> {

    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId;

    @NotNull(message = "차감할 수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    Integer quantity;

    @NotNull(message = "발생 일시는 필수입니다.")
    @PastOrPresent(message = "발생 일시는 현재보다 이후일 수 없습니다.")
    LocalDateTime occurrenceDateTime;

    public DeductStockCommand(Long productId, Integer quantity, LocalDateTime occurrenceDateTime) {
        this.productId = productId;
        this.quantity = quantity;
        this.occurrenceDateTime = occurrenceDateTime;
        this.validateSelf();
    }
}
