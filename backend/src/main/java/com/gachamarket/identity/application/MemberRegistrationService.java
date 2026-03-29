package com.gachamarket.identity.application;

import com.gachamarket.identity.domain.Member;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MemberRegistrationService {

    private final JdbcTemplate jdbcTemplate;
    private final NicknameGenerator nicknameGenerator;

    public MemberRegistrationService(JdbcTemplate jdbcTemplate, NicknameGenerator nicknameGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.nicknameGenerator = nicknameGenerator;
    }

    public Member registerOrLoad(String email) {
        Member existing = jdbcTemplate.query(
            """
                select m.id, m.email, m.nickname, w.current_point
                from members m
                join wallets w on w.member_id = m.id
                where m.email = ?
                """,
            rs -> rs.next()
                ? new Member(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("email"),
                    rs.getString("nickname"),
                    rs.getInt("current_point")
                )
                : null,
            email
        );

        if (existing != null) {
            return existing;
        }

        Instant now = Instant.now();
        Member member = new Member(UUID.randomUUID(), email, nicknameGenerator.generate(), 1000);

        jdbcTemplate.update(
            """
                insert into members (id, email, nickname, created_at, updated_at)
                values (?, ?, ?, ?, ?)
                """,
            member.id(),
            member.email(),
            member.nickname(),
            Timestamp.from(now),
            Timestamp.from(now)
        );
        jdbcTemplate.update(
            """
                insert into wallets (member_id, current_point, updated_at)
                values (?, ?, ?)
                """,
            member.id(),
            member.currentPoint(),
            Timestamp.from(now)
        );

        return member;
    }
}
