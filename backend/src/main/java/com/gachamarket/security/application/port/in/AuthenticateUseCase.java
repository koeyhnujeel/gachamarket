package com.gachamarket.security.application.port.in;

import com.gachamarket.security.domain.AuthUser;

public interface AuthenticateUseCase {

    AuthUser authenticate(String accessToken);
}
