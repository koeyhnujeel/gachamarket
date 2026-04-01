package com.gachamarket.identity.domain;

import com.gachamarket.shared.Role;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Member {

    private Long id;
    private final String email;
    private Nickname nickname;
    private final Role role;
    private Points points;
    private LocalDate lastFreeChargeDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Member(String email, Nickname nickname, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.points = Points.zero();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Member(Long id, String email, Nickname nickname, Role role, Points points,
                   LocalDate lastFreeChargeDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.points = points;
        this.lastFreeChargeDate = lastFreeChargeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateNickname(Nickname nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void chargeFreePoints() {
        LocalDate today = LocalDate.now();
        if (lastFreeChargeDate != null && lastFreeChargeDate.equals(today)) {
            throw new FreeChargeNotAllowedException();
        }
        this.points = points.charge(Points.FREE_CHARGE_AMOUNT);
        this.lastFreeChargeDate = today;
        this.updatedAt = LocalDateTime.now();
    }

    public void deductPoints(long amount) {
        if (!points.canBet(amount)) {
            throw new InsufficientPointsException();
        }
        this.points = points.deduct(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void addPoints(long amount) {
        this.points = points.charge(amount);
        this.updatedAt = LocalDateTime.now();
    }
}
