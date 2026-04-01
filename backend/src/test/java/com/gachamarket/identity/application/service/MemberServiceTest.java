package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.command.UpdateNicknameCommand;
import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.MemberNotFoundException;
import com.gachamarket.identity.domain.Nickname;
import com.gachamarket.identity.domain.NicknameAlreadyExistsException;
import com.gachamarket.shared.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void getProfileReturnsMemberInfo() {
        Member member = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberProfileResult result = memberService.getProfile(1L);

        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.nickname()).isEqualTo("테스트");
        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    void getProfileThrowsWhenMemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getProfile(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void updateNicknameSuccessfully() {
        Member member = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("새닉네임")).thenReturn(false);

        MemberProfileResult result = memberService.updateNickname(
                new UpdateNicknameCommand(1L, "새닉네임")
        );

        assertThat(result.nickname()).isEqualTo("새닉네임");
        verify(memberRepository).save(member);
    }

    @Test
    void updateSameNicknameWithoutDuplicateCheck() {
        Member member = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        memberService.updateNickname(new UpdateNicknameCommand(1L, "테스트"));

        verify(memberRepository, never()).existsByNickname(anyString());
        verify(memberRepository).save(member);
    }

    @Test
    void updateNicknameThrowsWhenDuplicate() {
        Member member = createMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname("중복닉네임")).thenReturn(true);

        assertThatThrownBy(() -> memberService.updateNickname(
                new UpdateNicknameCommand(1L, "중복닉네임")
        )).isInstanceOf(NicknameAlreadyExistsException.class);

        verify(memberRepository, never()).save(any());
    }

    private Member createMember() {
        return new Member(1L, "test@example.com", new Nickname("테스트"),
                Role.USER, com.gachamarket.identity.domain.Points.zero(),
                null, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    }
}
