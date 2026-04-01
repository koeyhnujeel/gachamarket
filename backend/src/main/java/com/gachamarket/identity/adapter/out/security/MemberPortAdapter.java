package com.gachamarket.identity.adapter.out.security;

import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.Nickname;
import com.gachamarket.security.application.port.out.MemberPort;
import com.gachamarket.shared.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPortAdapter implements MemberPort {

    private final MemberRepository memberRepository;

    @Override
    public Optional<MemberInfo> findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(this::toMemberInfo);
    }

    @Override
    public MemberInfo create(String email, String nickname, Role role) {
        Member member = new Member(email, new Nickname(nickname), role);
        Member saved = memberRepository.save(member);
        return toMemberInfo(saved);
    }

    private MemberInfo toMemberInfo(Member member) {
        return new MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getNickname().value(),
                member.getRole()
        );
    }
}
