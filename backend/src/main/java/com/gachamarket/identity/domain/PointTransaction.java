package com.gachamarket.identity.domain;

import java.time.LocalDateTime;

public class PointTransaction {

    private Long id;
    private final Long memberId;
    private final TransactionType type;
    private final long amount;
    private final Long referenceId;
    private final LocalDateTime createdAt;

    public PointTransaction(Long memberId, TransactionType type, long amount, Long referenceId) {
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
    }

    public PointTransaction(Long id, Long memberId, TransactionType type, long amount, Long referenceId, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public TransactionType getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
