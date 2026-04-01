package com.gachamarket.identity.application.dto.result;

public record PointTransactionResult(
        Long id,
        String type,
        long amount,
        Long referenceId,
        String createdAt
) {
}
