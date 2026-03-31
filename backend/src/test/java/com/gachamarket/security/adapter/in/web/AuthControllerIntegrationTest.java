package com.gachamarket.security.adapter.in.web;

import com.gachamarket.security.adapter.out.security.JwtAuthenticationFilter;
import com.gachamarket.security.adapter.out.security.JwtTokenProvider;
import com.gachamarket.support.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest {

    private static final String SECRET = "test-jwt-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION = 86400000L;

    private MockMvc mockMvc;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        AuthController controller = new AuthController();

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
    void meReturnsMemberIdWithValidToken() throws Exception {
        String token = jwtTokenProvider.createToken(1L, "test@example.com", "USER");

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1));
    }

    @Test
    void meReturnsErrorWhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void logoutClearsCookie() throws Exception {
        String token = jwtTokenProvider.createToken(1L, "test@example.com", "USER");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(cookie().maxAge("access_token", 0));
    }
}
