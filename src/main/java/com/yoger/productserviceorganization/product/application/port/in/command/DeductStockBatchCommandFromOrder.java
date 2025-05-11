package com.yoger.productserviceorganization.product.application.port.in.command;

import com.yoger.productserviceorganization.global.validator.SelfValidating;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class DeductStockBatchCommandFromOrder extends SelfValidating<DeductStockBatchCommandFromOrder> {

    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId;

    List<DeductStockCommandFromOrder> deductStockCommands;

    public DeductStockBatchCommandFromOrder(Long productId, List<DeductStockCommandFromOrder> deductStockCommands) {
        this.productId = productId;
        this.deductStockCommands = deductStockCommands;
        this.validateSelf();
    }
}
