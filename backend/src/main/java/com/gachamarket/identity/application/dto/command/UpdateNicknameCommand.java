package com.gachamarket.identity.application.dto.command;

public record UpdateNicknameCommand(
        Long memberId,
        String nickname
) {
}
