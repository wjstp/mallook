package io.ssafy.mallook.domain.orders.dto.request;

import io.ssafy.mallook.domain.member.entity.Member;
import io.ssafy.mallook.domain.orders.entity.Orders;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateDto(
        @NotNull
        @Min(100)
        Long price,

        @NotNull
        @Min(1)
        Long quantity,

        @NotNull
        Long fee) {

    public Orders toEntity(Member member) {
        return Orders.builder()
                .member(member)
                .totalPrice(this.price * this.quantity)
                .totalCount(quantity)
                .totalFee(fee)
                .build();
    }
}
