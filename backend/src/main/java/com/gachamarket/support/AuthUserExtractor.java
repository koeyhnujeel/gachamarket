package com.gachamarket.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUserExtractor {

    private AuthUserExtractor() {
    }

    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long memberId) {
            return memberId;
        }
        return null;
    }

    public static Long getRequiredMemberId() {
        Long memberId = getCurrentMemberId();
        if (memberId == null) {
            throw new IllegalStateException("인증된 사용자가 아닙니다.");
        }
        return memberId;
    }
}
