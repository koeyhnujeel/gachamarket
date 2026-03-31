package com.gachamarket.security.application.port.out;

import com.gachamarket.security.domain.AuthUser;

import java.util.Optional;

public interface OAuth2UserRepository {

    Optional<AuthUser> findByEmail(String email);

    AuthUser save(String email);
}
