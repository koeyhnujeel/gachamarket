package com.gachamarket.security.application.port.out;

import com.gachamarket.shared.Role;

import java.util.Optional;

/**
 * identity 모듈 소유 포트.
 * OAuth2 로그인 시 회원 조회/생성에 사용.
 * Phase 3에서 identity 모듈이 구현 예정.
 */
public interface MemberPort {

    Optional<MemberInfo> findByEmail(String email);

    MemberInfo create(String email, String nickname, Role role);

    record MemberInfo(Long id, String email, String nickname, Role role) {
    }
}
