package com.gachamarket.identity.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Wallet {

    private final UUID memberId;
    private final int currentPoint;

    public static Wallet open(UUID memberId, int currentPoint) {
        return new Wallet(memberId, currentPoint);
    }
}
