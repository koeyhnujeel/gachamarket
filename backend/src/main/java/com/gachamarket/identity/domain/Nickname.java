package com.gachamarket.identity.domain;

import java.util.concurrent.ThreadLocalRandom;

public record Nickname(String value) {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 20;

    public Nickname {
        validate(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "닉네임은 %d~%d자여야 합니다.".formatted(MIN_LENGTH, MAX_LENGTH)
            );
        }
    }

    public static Nickname generate() {
        return new Nickname("user" + ThreadLocalRandom.current().nextInt(100000, 999999));
    }
}
