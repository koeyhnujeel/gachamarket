package com.gachamarket.identity.domain;

import com.gachamarket.shared.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MemberTest {

    @Test
    void createMember() {
        Member member = new Member("test@example.com", new Nickname("테스트"), Role.USER);

        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getNickname().value()).isEqualTo("테스트");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getPoints().value()).isZero();
        assertThat(member.getLastFreeChargeDate()).isNull();
        assertThat(member.getCreatedAt()).isNotNull();
    }

    @Test
    void reconstructMember() {
        Member member = new Member(
                1L, "test@example.com", new Nickname("테스트"), Role.USER,
                Points.of(5000), null, null, null
        );

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getPoints().value()).isEqualTo(5000);
    }

    @Test
    void updateNickname() {
        Member member = createDefaultMember();
        member.updateNickname(new Nickname("새닉네임"));

        assertThat(member.getNickname().value()).isEqualTo("새닉네임");
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    void chargeFreePoints() {
        Member member = createDefaultMember();
        assertThat(member.getPoints().value()).isZero();

        member.chargeFreePoints();

        assertThat(member.getPoints().value()).isEqualTo(Points.FREE_CHARGE_AMOUNT);
        assertThat(member.getLastFreeChargeDate()).isNotNull();
    }

    @Test
    void throwWhenChargeFreePointsTwiceInSameDay() {
        Member member = createDefaultMember();
        member.chargeFreePoints();

        assertThatThrownBy(member::chargeFreePoints)
                .isInstanceOf(FreeChargeNotAllowedException.class);
    }

    @Test
    void deductPoints() {
        Member member = new Member(
                1L, "test@example.com", new Nickname("테스트"), Role.USER,
                Points.of(1000), null, null, null
        );

        member.deductPoints(100);

        assertThat(member.getPoints().value()).isEqualTo(900);
    }

    @Test
    void throwWhenDeductMoreThanBalance() {
        Member member = createDefaultMember();

        assertThatThrownBy(() -> member.deductPoints(10))
                .isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    void addPoints() {
        Member member = createDefaultMember();
        member.addPoints(500);

        assertThat(member.getPoints().value()).isEqualTo(500);
    }

    private Member createDefaultMember() {
        return new Member("test@example.com", new Nickname("테스트"), Role.USER);
    }
}
