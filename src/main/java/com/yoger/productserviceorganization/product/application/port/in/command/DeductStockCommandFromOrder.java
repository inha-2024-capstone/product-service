package com.yoger.productserviceorganization.product.application.port.in.command;

import com.yoger.productserviceorganization.global.validator.SelfValidating;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class DeductStockCommandFromOrder extends SelfValidating<DeductStockCommandFromOrder> {

    @NotNull(message = "주문 ID는 필수입니다.")
    Long orderId;

    @NotNull(message = "재고 차감 명령은 필수입니다.")
    @Valid
    DeductStockCommand deductStockCommand;

    public DeductStockCommandFromOrder(Long orderId, DeductStockCommand deductStockCommand) {
        this.orderId = orderId;
        this.deductStockCommand = deductStockCommand;
        this.validateSelf();
    }
}
