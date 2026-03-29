package com.gachamarket.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void registersMemberWithFreeNicknameChangeAvailable() {
        Member member = Member.register(
            UUID.fromString("10000000-0000-0000-0000-000000000001"),
            "user@example.com",
            "GM-123456"
        );

        assertThat(member.email()).isEqualTo("user@example.com");
        assertThat(member.nickname()).isEqualTo("GM-123456");
        assertThat(member.nicknameChangeFreeUsed()).isFalse();
    }
}
