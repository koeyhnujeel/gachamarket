package com.gachamarket.identity.domain;

import java.util.UUID;

public record Wallet(UUID memberId, int currentPoint) {

    public static Wallet open(UUID memberId, int currentPoint) {
        return new Wallet(memberId, currentPoint);
    }
}
