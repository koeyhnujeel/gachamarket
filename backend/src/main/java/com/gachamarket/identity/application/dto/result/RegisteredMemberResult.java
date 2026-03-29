package com.gachamarket.identity.application.dto.result;

import java.util.UUID;

public record RegisteredMemberResult(UUID id, String email, String nickname, int currentPoint) {
}
