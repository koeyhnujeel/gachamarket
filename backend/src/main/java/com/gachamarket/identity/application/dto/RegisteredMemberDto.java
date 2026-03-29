package com.gachamarket.identity.application.dto;

import java.util.UUID;

public record RegisteredMemberDto(UUID id, String email, String nickname, int currentPoint) {
}
