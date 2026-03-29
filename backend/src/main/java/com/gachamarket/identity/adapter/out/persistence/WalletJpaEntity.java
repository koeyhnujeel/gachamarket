package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.domain.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletJpaEntity {

    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "current_point", nullable = false)
    private int currentPoint;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static WalletJpaEntity from(Wallet wallet, Instant now) {
        return new WalletJpaEntity(wallet.getMemberId(), wallet.getCurrentPoint(), now);
    }

    public Wallet toDomain() {
        return Wallet.open(memberId, currentPoint);
    }
}
