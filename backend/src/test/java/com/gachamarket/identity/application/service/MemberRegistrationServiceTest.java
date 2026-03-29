package com.gachamarket.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gachamarket.identity.application.dto.RegisteredMemberDto;
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
            new MemberAccount(
                Member.register(memberId, "user@example.com", "GM-111111"),
                new Wallet(memberId, 1000)
            )
        ));

        RegisteredMemberDto result = memberRegistrationService.registerOrLoad("user@example.com");

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

        RegisteredMemberDto result = memberRegistrationService.registerOrLoad("user@example.com");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(saveMemberPort).save(memberCaptor.capture(), walletCaptor.capture(), any(Instant.class));
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.nickname()).isEqualTo("GM-222222");
        assertThat(result.currentPoint()).isEqualTo(1000);
        assertThat(memberCaptor.getValue().nickname()).isEqualTo("GM-222222");
        assertThat(walletCaptor.getValue().currentPoint()).isEqualTo(1000);
    }
}
