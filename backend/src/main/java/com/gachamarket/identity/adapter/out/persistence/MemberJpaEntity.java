package com.gachamarket.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(nullable = false)
    private Long points;

    @Column(name = "last_free_charge_date")
    private LocalDate lastFreeChargeDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public MemberJpaEntity(String email, String nickname, String role, Long points,
                           LocalDate lastFreeChargeDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.points = points;
        this.lastFreeChargeDate = lastFreeChargeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public String getRole() { return role; }
    public Long getPoints() { return points; }
    public LocalDate getLastFreeChargeDate() { return lastFreeChargeDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateNickname(String nickname, LocalDateTime updatedAt) {
        this.nickname = nickname;
        this.updatedAt = updatedAt;
    }

    public void updatePoints(Long points, LocalDate lastFreeChargeDate, LocalDateTime updatedAt) {
        this.points = points;
        this.lastFreeChargeDate = lastFreeChargeDate;
        this.updatedAt = updatedAt;
    }
}
