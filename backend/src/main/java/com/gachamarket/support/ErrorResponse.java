package com.gachamarket.support;

public record ErrorResponse(
        String code,
        String message,
        String detail
) {
}
