package com.gachamarket.identity.adapter.in.web.dto.request;

import com.gachamarket.identity.application.dto.command.UpdateNicknameCommand;

public record UpdateNicknameRequest(
        String nickname
) {
    public UpdateNicknameCommand toCommand(Long memberId) {
        return new UpdateNicknameCommand(memberId, nickname);
    }
}
