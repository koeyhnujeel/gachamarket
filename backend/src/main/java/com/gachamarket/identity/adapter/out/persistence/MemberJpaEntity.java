package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.domain.Member;
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
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberJpaEntity {

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

    public static MemberJpaEntity from(Member member, Instant now) {
        return new MemberJpaEntity(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            null,
            member.isNicknameChangeFreeUsed(),
            now,
            now
        );
    }

    public Member toDomain() {
        return Member.of(id, email, nickname, nicknameChangeFreeUsed);
    }
}
