package com.gachamarket.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gachamarket.identity.application.dto.command.RegisterMemberCommand;
import com.gachamarket.identity.application.dto.result.RegisteredMemberResult;
import com.gachamarket.identity.application.port.out.GenerateNicknamePort;
import com.gachamarket.identity.application.port.out.LoadMemberPort;
import com.gachamarket.identity.application.port.out.SaveMemberPort;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.MemberAccount;
import com.gachamarket.identity.domain.Wallet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MemberRegistrationServiceTest {

    @Test
    void returnsExistingMemberWithoutCreatingNewOne() {
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        GenerateNicknamePort generateNicknamePort = mock(GenerateNicknamePort.class);
        MemberRegistrationService memberRegistrationService = new MemberRegistrationService(
            loadMemberPort,
            saveMemberPort,
            generateNicknamePort
        );

        UUID memberId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        when(loadMemberPort.loadByEmail("user@example.com")).thenReturn(Optional.of(
            MemberAccount.of(
                Member.register(memberId, "user@example.com", "GM-111111"),
                Wallet.open(memberId, 1000)
            )
        ));

        RegisteredMemberResult result = memberRegistrationService.registerOrLoad(
            new RegisterMemberCommand("user@example.com")
        );

        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.nickname()).isEqualTo("GM-111111");
        assertThat(result.currentPoint()).isEqualTo(1000);
        verifyNoInteractions(saveMemberPort, generateNicknamePort);
    }

    @Test
    void createsMemberAndWalletWhenEmailDoesNotExist() {
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        GenerateNicknamePort generateNicknamePort = mock(GenerateNicknamePort.class);
        MemberRegistrationService memberRegistrationService = new MemberRegistrationService(
            loadMemberPort,
            saveMemberPort,
            generateNicknamePort
        );

        when(loadMemberPort.loadByEmail("user@example.com")).thenReturn(Optional.empty());
        when(generateNicknamePort.generate()).thenReturn("GM-222222");

        RegisteredMemberResult result = memberRegistrationService.registerOrLoad(
            new RegisterMemberCommand("user@example.com")
        );

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(saveMemberPort).save(memberCaptor.capture(), walletCaptor.capture(), any(Instant.class));
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.nickname()).isEqualTo("GM-222222");
        assertThat(result.currentPoint()).isEqualTo(1000);
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("GM-222222");
        assertThat(walletCaptor.getValue().getCurrentPoint()).isEqualTo(1000);
    }
}
