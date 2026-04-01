package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.command.UpdateNicknameCommand;
import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.port.in.GetMemberProfileUseCase;
import com.gachamarket.identity.application.port.in.UpdateNicknameUseCase;
import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.Nickname;
import com.gachamarket.identity.domain.NicknameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService implements GetMemberProfileUseCase, UpdateNicknameUseCase {

    private final MemberRepository memberRepository;

    @Override
    public MemberProfileResult getProfile(Long memberId) {
        Member member = findMember(memberId);
        return toProfileResult(member);
    }

    @Override
    @Transactional
    public MemberProfileResult updateNickname(UpdateNicknameCommand command) {
        Member member = findMember(command.memberId());
        Nickname newNickname = new Nickname(command.nickname());

        if (!member.getNickname().value().equals(command.nickname())
                && memberRepository.existsByNickname(command.nickname())) {
            throw new NicknameAlreadyExistsException();
        }

        member.updateNickname(newNickname);
        memberRepository.save(member);
        return toProfileResult(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new com.gachamarket.identity.domain.MemberNotFoundException());
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
}
