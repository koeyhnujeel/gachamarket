package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.RegisteredMemberDto;

public interface RegisterMemberUseCase {

    RegisteredMemberDto registerOrLoad(String email);
}
