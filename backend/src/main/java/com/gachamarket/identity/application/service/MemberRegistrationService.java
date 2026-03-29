package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.RegisteredMemberDto;
import com.gachamarket.identity.application.port.in.RegisterMemberUseCase;
import com.gachamarket.identity.application.port.out.GenerateNicknamePort;
import com.gachamarket.identity.application.port.out.LoadMemberPort;
import com.gachamarket.identity.application.port.out.SaveMemberPort;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.Wallet;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberRegistrationService implements RegisterMemberUseCase {

    private static final int INITIAL_POINT = 1000;

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final GenerateNicknamePort generateNicknamePort;

    public MemberRegistrationService(
        LoadMemberPort loadMemberPort,
        SaveMemberPort saveMemberPort,
        GenerateNicknamePort generateNicknamePort
    ) {
        this.loadMemberPort = loadMemberPort;
        this.saveMemberPort = saveMemberPort;
        this.generateNicknamePort = generateNicknamePort;
    }

    @Override
    @Transactional
    public RegisteredMemberDto registerOrLoad(String email) {
        return loadMemberPort.loadByEmail(email)
            .map(memberAccount -> new RegisteredMemberDto(
                memberAccount.member().id(),
                memberAccount.member().email(),
                memberAccount.member().nickname(),
                memberAccount.wallet().currentPoint()
            ))
            .orElseGet(() -> registerNewMember(email));
    }

    private RegisteredMemberDto registerNewMember(String email) {
        Member member = Member.register(UUID.randomUUID(), email, generateNicknamePort.generate());
        Wallet wallet = Wallet.open(member.id(), INITIAL_POINT);

        saveMemberPort.save(member, wallet, Instant.now());

        return new RegisteredMemberDto(member.id(), member.email(), member.nickname(), wallet.currentPoint());
    }
}
