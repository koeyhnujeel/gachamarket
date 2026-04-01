package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.dto.command.UpdateNicknameCommand;

public interface UpdateNicknameUseCase {

    MemberProfileResult updateNickname(UpdateNicknameCommand command);
}
