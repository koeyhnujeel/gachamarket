package com.gachamarket.security.domain;

import com.gachamarket.shared.Role;

public record AuthUser(
        Long memberId,
        String email,
        Role role
) {
}
