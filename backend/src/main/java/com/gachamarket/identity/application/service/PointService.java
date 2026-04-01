package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.dto.result.PointTransactionResult;
import com.gachamarket.identity.application.port.in.ChargeFreePointsUseCase;
import com.gachamarket.identity.application.port.in.GetPointHistoryUseCase;
import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.application.port.out.PointTransactionRepository;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.MemberNotFoundException;
import com.gachamarket.identity.domain.PointTransaction;
import com.gachamarket.identity.domain.TransactionType;
import com.gachamarket.support.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService implements ChargeFreePointsUseCase, GetPointHistoryUseCase {

    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    @Transactional
    public MemberProfileResult chargeFreePoints(Long memberId) {
        Member member = findMember(memberId);
        member.chargeFreePoints();
        memberRepository.save(member);

        PointTransaction transaction = new PointTransaction(
                memberId, TransactionType.FREE_CHARGE,
                com.gachamarket.identity.domain.Points.FREE_CHARGE_AMOUNT, null
        );
        pointTransactionRepository.save(transaction);

        return toProfileResult(member);
    }

    @Override
    public PageResponse<PointTransactionResult> getHistory(Long memberId, int page, int size) {
        PageResponse<PointTransaction> pageResult =
                pointTransactionRepository.findByMemberIdOrderByCreatedAtDesc(memberId, page, size);

        var results = pageResult.content().stream()
                .map(this::toTransactionResult)
                .toList();

        return new PageResponse<>(
                results, pageResult.page(), pageResult.size(),
                pageResult.totalElements(), pageResult.totalPages()
        );
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private MemberProfileResult toProfileResult(Member member) {
        return new MemberProfileResult(
                member.getId(),
                member.getEmail(),
                member.getNickname().value(),
                member.getRole().name(),
                member.getPoints().value(),
                member.getLastFreeChargeDate() != null ? member.getLastFreeChargeDate().toString() : null
        );
    }

    private PointTransactionResult toTransactionResult(PointTransaction tx) {
        return new PointTransactionResult(
                tx.getId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getReferenceId(),
                tx.getCreatedAt().toString()
        );
    }
}
