package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.domain.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class JpaWalletEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "current_point", nullable = false)
    private int currentPoint;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JpaWalletEntity() {
    }

    private JpaWalletEntity(UUID memberId, int currentPoint, Instant updatedAt) {
        this.memberId = memberId;
        this.currentPoint = currentPoint;
        this.updatedAt = updatedAt;
    }

    public static JpaWalletEntity from(Wallet wallet, Instant now) {
        return new JpaWalletEntity(wallet.memberId(), wallet.currentPoint(), now);
    }

    public Wallet toDomain() {
        return new Wallet(memberId, currentPoint);
    }
}
