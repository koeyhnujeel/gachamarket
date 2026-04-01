package com.gachamarket.identity.domain;

public record Points(long value) {

    public static final long MINIMUM_BET_AMOUNT = 10L;
    public static final long FREE_CHARGE_AMOUNT = 1000L;

    public Points {
        if (value < 0) {
            throw new IllegalArgumentException("포인트는 음수일 수 없습니다.");
        }
    }

    public static Points zero() {
        return new Points(0L);
    }

    public static Points of(long value) {
        return new Points(value);
    }

    public boolean canBet(long amount) {
        return value >= amount && amount >= MINIMUM_BET_AMOUNT;
    }

    public boolean isZero() {
        return value == 0;
    }

    public Points charge(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 양수여야 합니다.");
        }
        return new Points(value + amount);
    }

    public Points deduct(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 금액은 양수여야 합니다.");
        }
        if (value < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        return new Points(value - amount);
    }
}
