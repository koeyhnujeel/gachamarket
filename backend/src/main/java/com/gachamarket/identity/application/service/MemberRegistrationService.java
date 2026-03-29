package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.command.RegisterMemberCommand;
import com.gachamarket.identity.application.dto.result.RegisteredMemberResult;
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
    public RegisteredMemberResult registerOrLoad(RegisterMemberCommand command) {
        return loadMemberPort.loadByEmail(command.email())
            .map(memberAccount -> new RegisteredMemberResult(
                memberAccount.getMember().getId(),
                memberAccount.getMember().getEmail(),
                memberAccount.getMember().getNickname(),
                memberAccount.getWallet().getCurrentPoint()
            ))
            .orElseGet(() -> registerNewMember(command.email()));
    }

    private RegisteredMemberResult registerNewMember(String email) {
        Member member = Member.register(UUID.randomUUID(), email, generateNicknamePort.generate());
        Wallet wallet = Wallet.open(member.getId(), INITIAL_POINT);

        saveMemberPort.save(member, wallet, Instant.now());

        return new RegisteredMemberResult(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            wallet.getCurrentPoint()
        );
    }
}
