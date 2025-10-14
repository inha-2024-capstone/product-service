package com.yoger.productserviceorganization.product.application.port.in.command;

import com.yoger.productserviceorganization.global.validator.SelfValidating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class DeductStockFromOrderCommand extends SelfValidating<DeductStockFromOrderCommand> {

    @NotBlank(message = "주문 ID는 필수입니다.")
    String orderId;

    @NotBlank(message = "이벤트 ID는 필수입니다.")
    String eventId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId;

    @NotEmpty(message = "아이템 목록은 비어 있을 수 없습니다.")
    List<OrderItem> items;

    @NotNull(message = "발생 일시는 필수입니다.")
    @PastOrPresent(message = "발생 일시는 현재보다 이후일 수 없습니다.")
    LocalDateTime occurrenceDateTime;

    public DeductStockFromOrderCommand(
            String orderId,
            String eventId,
            Long userId,
            List<OrderItem> items,
            LocalDateTime occurrenceDateTime
    ) {
        this.orderId = orderId;
        this.eventId = eventId;
        this.userId = userId;
        this.items = items;
        this.occurrenceDateTime = occurrenceDateTime;
        this.validateSelf();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static class OrderItem extends SelfValidating<OrderItem> {
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity;

        public OrderItem(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
            this.validateSelf();
        }
    }
}
