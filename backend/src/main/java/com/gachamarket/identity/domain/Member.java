package com.gachamarket.identity.domain;

import java.util.UUID;

public record Member(UUID id, String email, String nickname, boolean nicknameChangeFreeUsed) {

    public static Member register(UUID id, String email, String nickname) {
        return new Member(id, email, nickname, false);
    }
}
