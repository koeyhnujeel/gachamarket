package com.gachamarket.identity.application.service;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.dto.result.PointTransactionResult;
import com.gachamarket.identity.application.port.out.MemberRepository;
import com.gachamarket.identity.application.port.out.PointTransactionRepository;
import com.gachamarket.identity.domain.*;
import com.gachamarket.shared.Role;
import com.gachamarket.support.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void chargeFreePointsSuccessfully() {
        Member member = createMemberWithZeroPoints();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberProfileResult result = pointService.chargeFreePoints(1L);

        assertThat(result.points()).isEqualTo(Points.FREE_CHARGE_AMOUNT);
        verify(memberRepository).save(member);
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    void chargeFreePointsThrowsWhenAlreadyCharged() {
        Member member = createMemberWithZeroPoints();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        pointService.chargeFreePoints(1L);

        assertThatThrownBy(() -> pointService.chargeFreePoints(1L))
                .isInstanceOf(FreeChargeNotAllowedException.class);
    }

    @Test
    void chargeFreePointsThrowsWhenMemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.chargeFreePoints(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void getHistoryReturnsPaginatedResults() {
        PointTransaction tx = new PointTransaction(
                1L, 1L, TransactionType.FREE_CHARGE, 1000L, null, LocalDateTime.now()
        );
        PageResponse<PointTransaction> pageResponse = new PageResponse<>(
                List.of(tx), 0, 10, 1, 1
        );
        when(pointTransactionRepository.findByMemberIdOrderByCreatedAtDesc(1L, 0, 10))
                .thenReturn(pageResponse);

        PageResponse<PointTransactionResult> result = pointService.getHistory(1L, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).type()).isEqualTo("FREE_CHARGE");
        assertThat(result.content().get(0).amount()).isEqualTo(1000L);
    }

    private Member createMemberWithZeroPoints() {
        return new Member(1L, "test@example.com", new Nickname("테스트"),
                Role.USER, Points.zero(), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
