package com.gachamarket.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointsTest {

    @Test
    void createWithPositiveValue() {
        Points points = new Points(1000);
        assertThat(points.value()).isEqualTo(1000);
    }

    @Test
    void createWithZero() {
        Points points = Points.zero();
        assertThat(points.value()).isZero();
        assertThat(points.isZero()).isTrue();
    }

    @Test
    void throwWhenNegative() {
        assertThatThrownBy(() -> new Points(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("음수");
    }

    @Test
    void chargePoints() {
        Points points = new Points(100);
        Points charged = points.charge(500);
        assertThat(charged.value()).isEqualTo(600);
    }

    @Test
    void throwWhenChargeNonPositive() {
        Points points = new Points(100);
        assertThatThrownBy(() -> points.charge(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> points.charge(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deductPoints() {
        Points points = new Points(500);
        Points deducted = points.deduct(200);
        assertThat(deducted.value()).isEqualTo(300);
    }

    @Test
    void throwWhenDeductMoreThanBalance() {
        Points points = new Points(100);
        assertThatThrownBy(() -> points.deduct(200))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("부족");
    }

    @Test
    void throwWhenDeductNonPositive() {
        Points points = new Points(100);
        assertThatThrownBy(() -> points.deduct(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void canBetWhenSufficientBalance() {
        Points points = new Points(100);
        assertThat(points.canBet(10)).isTrue();
        assertThat(points.canBet(100)).isTrue();
    }

    @Test
    void cannotBetWhenInsufficientBalance() {
        Points points = new Points(5);
        assertThat(points.canBet(10)).isFalse();
    }

    @Test
    void cannotBetBelowMinimum() {
        Points points = new Points(1000);
        assertThat(points.canBet(9)).isFalse();
    }

    @Test
    void isZeroReturnsFalseForPositiveBalance() {
        assertThat(new Points(1).isZero()).isFalse();
    }
}
