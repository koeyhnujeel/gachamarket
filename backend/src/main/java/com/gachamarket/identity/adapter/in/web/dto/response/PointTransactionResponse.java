package com.gachamarket.identity.adapter.in.web.dto.response;

import com.gachamarket.identity.application.dto.result.PointTransactionResult;

public record PointTransactionResponse(
        Long id,
        String type,
        long amount,
        Long referenceId,
        String createdAt
) {
    public static PointTransactionResponse from(PointTransactionResult result) {
        return new PointTransactionResponse(
                result.id(), result.type(), result.amount(),
                result.referenceId(), result.createdAt()
        );
    }
}
