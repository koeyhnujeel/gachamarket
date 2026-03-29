package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "members")
public class JpaMemberEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 32)
    private String nickname;

    @Column(name = "active_title_id")
    private UUID activeTitleId;

    @Column(name = "nickname_change_free_used", nullable = false)
    private boolean nicknameChangeFreeUsed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected JpaMemberEntity() {
    }

    private JpaMemberEntity(
        UUID id,
        String email,
        String nickname,
        UUID activeTitleId,
        boolean nicknameChangeFreeUsed,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.activeTitleId = activeTitleId;
        this.nicknameChangeFreeUsed = nicknameChangeFreeUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JpaMemberEntity from(Member member, Instant now) {
        return new JpaMemberEntity(
            member.id(),
            member.email(),
            member.nickname(),
            null,
            member.nicknameChangeFreeUsed(),
            now,
            now
        );
    }

    public Member toDomain() {
        return new Member(id, email, nickname, nicknameChangeFreeUsed);
    }

    public UUID getId() {
        return id;
    }
}
