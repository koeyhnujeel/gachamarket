package com.gachamarket.identity.adapter.in.web.dto.response;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;

public record MemberProfileResponse(
        Long id,
        String email,
        String nickname,
        String role,
        long points,
        String lastFreeChargeDate
) {
    public static MemberProfileResponse from(MemberProfileResult result) {
        return new MemberProfileResponse(
                result.id(), result.email(), result.nickname(),
                result.role(), result.points(), result.lastFreeChargeDate()
        );
    }
}
