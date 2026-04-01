package com.gachamarket.identity.application.dto.result;

public record MemberProfileResult(
        Long id,
        String email,
        String nickname,
        String role,
        long points,
        String lastFreeChargeDate
) {
}
