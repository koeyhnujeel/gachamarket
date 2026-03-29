package com.gachamarket.identity.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    private final UUID id;
    private final String email;
    private final String nickname;
    private final boolean nicknameChangeFreeUsed;

    public static Member register(UUID id, String email, String nickname) {
        return new Member(id, email, nickname, false);
    }

    public static Member of(UUID id, String email, String nickname, boolean nicknameChangeFreeUsed) {
        return new Member(id, email, nickname, nicknameChangeFreeUsed);
    }
}
