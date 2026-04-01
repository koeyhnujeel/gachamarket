package com.gachamarket.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NicknameTest {

    @Test
    void createValidNickname() {
        Nickname nickname = new Nickname("테스트유저");
        assertThat(nickname.value()).isEqualTo("테스트유저");
    }

    @Test
    void createWithMinLength() {
        assertThatCode(() -> new Nickname("ab")).doesNotThrowAnyException();
    }

    @Test
    void createWithMaxLength() {
        assertThatCode(() -> new Nickname("a".repeat(20))).doesNotThrowAnyException();
    }

    @Test
    void throwWhenTooShort() {
        assertThatThrownBy(() -> new Nickname("a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2~20자");
    }

    @Test
    void throwWhenTooLong() {
        assertThatThrownBy(() -> new Nickname("a".repeat(21)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2~20자");
    }

    @Test
    void throwWhenNull() {
        assertThatThrownBy(() -> new Nickname(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwWhenBlank() {
        assertThatThrownBy(() -> new Nickname("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateCreatesValidNickname() {
        Nickname nickname = Nickname.generate();
        assertThat(nickname.value()).startsWith("user");
        assertThat(nickname.value().length()).isGreaterThanOrEqualTo(2);
    }
}
