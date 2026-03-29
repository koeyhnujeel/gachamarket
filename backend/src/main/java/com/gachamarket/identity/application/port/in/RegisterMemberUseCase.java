package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.command.RegisterMemberCommand;
import com.gachamarket.identity.application.dto.result.RegisteredMemberResult;

public interface RegisterMemberUseCase {

    RegisteredMemberResult registerOrLoad(RegisterMemberCommand command);
}
