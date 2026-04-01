package com.gachamarket.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PointTransactionJpaEntity(Long memberId, String type, Long amount,
                                     Long referenceId, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getType() { return type; }
    public Long getAmount() { return amount; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
