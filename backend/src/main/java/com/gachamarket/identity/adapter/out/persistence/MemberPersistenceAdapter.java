package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.application.port.out.PointTransactionRepository;
import com.gachamarket.identity.domain.*;
import com.gachamarket.shared.Role;
import com.gachamarket.support.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberRepository, PointTransactionRepository {

    private final MemberJpaRepository memberJpaRepository;
    private final PointTransactionJpaRepository pointTransactionJpaRepository;

    // -- MemberRepository --

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = toEntity(member);
        MemberJpaEntity saved = memberJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberJpaRepository.existsByNickname(nickname);
    }

    // -- PointTransactionRepository --

    @Override
    public PointTransaction save(PointTransaction transaction) {
        PointTransactionJpaEntity entity = toEntity(transaction);
        PointTransactionJpaEntity saved = pointTransactionJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public PageResponse<PointTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, int page, int size) {
        Page<PointTransactionJpaEntity> pageResult =
                pointTransactionJpaRepository.findByMemberIdOrderByCreatedAtDesc(
                        memberId, PageRequest.of(page, size)
                );
        var content = pageResult.getContent().stream().map(this::toDomain).toList();
        return new PageResponse<>(
                content, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages()
        );
    }

    // -- Conversion --

    private MemberJpaEntity toEntity(Member member) {
        if (member.getId() == null) {
            return new MemberJpaEntity(
                    member.getEmail(),
                    member.getNickname().value(),
                    member.getRole().name(),
                    member.getPoints().value(),
                    member.getLastFreeChargeDate(),
                    member.getCreatedAt(),
                    member.getUpdatedAt()
            );
        }
        MemberJpaEntity entity = memberJpaRepository.findById(member.getId())
                .orElse(new MemberJpaEntity(
                        member.getEmail(),
                        member.getNickname().value(),
                        member.getRole().name(),
                        member.getPoints().value(),
                        member.getLastFreeChargeDate(),
                        member.getCreatedAt(),
                        member.getUpdatedAt()
                ));
        entity.updateNickname(member.getNickname().value(), member.getUpdatedAt());
        entity.updatePoints(member.getPoints().value(), member.getLastFreeChargeDate(), member.getUpdatedAt());
        return entity;
    }

    private Member toDomain(MemberJpaEntity entity) {
        return new Member(
                entity.getId(),
                entity.getEmail(),
                new Nickname(entity.getNickname()),
                Role.valueOf(entity.getRole()),
                Points.of(entity.getPoints()),
                entity.getLastFreeChargeDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PointTransactionJpaEntity toEntity(PointTransaction tx) {
        return new PointTransactionJpaEntity(
                tx.getMemberId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getReferenceId(),
                tx.getCreatedAt()
        );
    }

    private PointTransaction toDomain(PointTransactionJpaEntity entity) {
        return new PointTransaction(
                entity.getId(),
                entity.getMemberId(),
                TransactionType.valueOf(entity.getType()),
                entity.getAmount(),
                entity.getReferenceId(),
                entity.getCreatedAt()
        );
    }
}
