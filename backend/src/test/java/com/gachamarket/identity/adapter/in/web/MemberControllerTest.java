package com.gachamarket.identity.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachamarket.identity.adapter.in.web.dto.request.UpdateNicknameRequest;
import com.gachamarket.identity.application.dto.result.MemberProfileResult;
import com.gachamarket.identity.application.dto.result.PointTransactionResult;
import com.gachamarket.identity.application.port.in.ChargeFreePointsUseCase;
import com.gachamarket.identity.application.port.in.GetMemberProfileUseCase;
import com.gachamarket.identity.application.port.in.GetPointHistoryUseCase;
import com.gachamarket.identity.application.port.in.UpdateNicknameUseCase;
import com.gachamarket.identity.application.dto.command.UpdateNicknameCommand;
import com.gachamarket.security.adapter.out.security.JwtAuthenticationFilter;
import com.gachamarket.security.adapter.out.security.JwtTokenProvider;
import com.gachamarket.support.GlobalExceptionHandler;
import com.gachamarket.support.PageResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTest {

    private static final String SECRET = "test-jwt-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION = 86400000L;

    private MockMvc mockMvc;
    private JwtTokenProvider jwtTokenProvider;
    private GetMemberProfileUseCase getMemberProfileUseCase;
    private UpdateNicknameUseCase updateNicknameUseCase;
    private ChargeFreePointsUseCase chargeFreePointsUseCase;
    private GetPointHistoryUseCase getPointHistoryUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
        getMemberProfileUseCase = mock(GetMemberProfileUseCase.class);
        updateNicknameUseCase = mock(UpdateNicknameUseCase.class);
        chargeFreePointsUseCase = mock(ChargeFreePointsUseCase.class);
        getPointHistoryUseCase = mock(GetPointHistoryUseCase.class);
        objectMapper = new ObjectMapper();

        MemberController controller = new MemberController(
                getMemberProfileUseCase, updateNicknameUseCase,
                chargeFreePointsUseCase, getPointHistoryUseCase
        );

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfileReturnsMemberInfo() throws Exception {
        String token = createToken(1L);
        when(getMemberProfileUseCase.getProfile(1L))
                .thenReturn(new MemberProfileResult(1L, "test@example.com", "테스트", "USER", 0, null));

        mockMvc.perform(get("/api/members/me")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트"));
    }

    @Test
    void updateNicknameSuccessfully() throws Exception {
        String token = createToken(1L);
        when(updateNicknameUseCase.updateNickname(any(UpdateNicknameCommand.class)))
                .thenReturn(new MemberProfileResult(1L, "test@example.com", "새닉네임", "USER", 0, null));

        mockMvc.perform(patch("/api/members/me/nickname")
                        .cookie(new Cookie("access_token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateNicknameRequest("새닉네임"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    void chargeFreePointsSuccessfully() throws Exception {
        String token = createToken(1L);
        when(chargeFreePointsUseCase.chargeFreePoints(1L))
                .thenReturn(new MemberProfileResult(1L, "test@example.com", "충전테스트", "USER", 1000, "2026-04-01"));

        mockMvc.perform(post("/api/members/me/free-charge")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.points").value(1000));
    }

    @Test
    void getPointHistoryReturnsTransactions() throws Exception {
        String token = createToken(1L);
        PageResponse<PointTransactionResult> pageResponse = new PageResponse<>(
                List.of(new PointTransactionResult(1L, "FREE_CHARGE", 1000L, null, LocalDateTime.now().toString())),
                0, 10, 1, 1
        );
        when(getPointHistoryUseCase.getHistory(1L, 0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/members/me/points")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    private String createToken(Long memberId) {
        return jwtTokenProvider.createToken(memberId, "test@example.com", "USER");
    }
}
