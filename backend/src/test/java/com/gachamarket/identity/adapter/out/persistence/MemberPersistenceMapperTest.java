package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.domain.*;
import com.gachamarket.shared.Role;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class MemberPersistenceMapperTest {

    @Test
    void domainToEntityAndBackPreservesAllFields() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDate chargeDate = LocalDate.now();
        Member member = new Member(
                1L, "map@test.com", new Nickname("매퍼테스트"), Role.USER,
                Points.of(5000), chargeDate, now, now
        );

        MemberJpaEntity entity = toEntity(member);

        assertThat(entity.getEmail()).isEqualTo("map@test.com");
        assertThat(entity.getNickname()).isEqualTo("매퍼테스트");
        assertThat(entity.getRole()).isEqualTo("USER");
        assertThat(entity.getPoints()).isEqualTo(5000);
        assertThat(entity.getLastFreeChargeDate()).isEqualTo(chargeDate);
    }

    @Test
    void entityToDomainPreservesAllFields() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDate chargeDate = LocalDate.now();
        MemberJpaEntity entity = createEntity(
                1L, "reverse@test.com", "역방향", "USER", 3000L, chargeDate, now, now
        );

        Member member = toDomain(entity);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getEmail()).isEqualTo("reverse@test.com");
        assertThat(member.getNickname().value()).isEqualTo("역방향");
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getPoints().value()).isEqualTo(3000);
        assertThat(member.getLastFreeChargeDate()).isEqualTo(chargeDate);
    }

    @Test
    void pointTransactionDomainToEntityAndBack() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        PointTransaction tx = new PointTransaction(1L, 2L, TransactionType.FREE_CHARGE, 1000L, null, now);

        PointTransactionJpaEntity entity = toTxEntity(tx);

        assertThat(entity.getMemberId()).isEqualTo(2L);
        assertThat(entity.getType()).isEqualTo("FREE_CHARGE");
        assertThat(entity.getAmount()).isEqualTo(1000L);
    }

    private MemberJpaEntity toEntity(Member member) throws Exception {
        return new MemberJpaEntity(
                member.getEmail(), member.getNickname().value(),
                member.getRole().name(), member.getPoints().value(),
                member.getLastFreeChargeDate(), member.getCreatedAt(), member.getUpdatedAt()
        );
    }

    private Member toDomain(MemberJpaEntity entity) {
        return new Member(
                entity.getId(), entity.getEmail(),
                new Nickname(entity.getNickname()),
                Role.valueOf(entity.getRole()),
                Points.of(entity.getPoints()),
                entity.getLastFreeChargeDate(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private PointTransactionJpaEntity toTxEntity(PointTransaction tx) {
        return new PointTransactionJpaEntity(
                tx.getMemberId(), tx.getType().name(),
                tx.getAmount(), tx.getReferenceId(), tx.getCreatedAt()
        );
    }

    private MemberJpaEntity createEntity(Long id, String email, String nickname,
                                          String role, Long points, LocalDate chargeDate,
                                          LocalDateTime createdAt, LocalDateTime updatedAt) throws Exception {
        Constructor<MemberJpaEntity> ctor = MemberJpaEntity.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        MemberJpaEntity entity = ctor.newInstance();
        setId(entity, id);
        setField(entity, "email", email);
        setField(entity, "nickname", nickname);
        setField(entity, "role", role);
        setField(entity, "points", points);
        setField(entity, "lastFreeChargeDate", chargeDate);
        setField(entity, "createdAt", createdAt);
        setField(entity, "updatedAt", updatedAt);
        return entity;
    }

    private void setId(Object entity, Long id) throws Exception {
        var field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private void setField(Object entity, String name, Object value) throws Exception {
        var field = entity.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
