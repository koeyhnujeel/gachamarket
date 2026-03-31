package com.gachamarket.security.adapter.out.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-jwt-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION = 86400000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void createsAndValidatesToken() {
        String token = jwtTokenProvider.createToken(1L, "test@example.com", "USER");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void extractsMemberIdFromToken() {
        String token = jwtTokenProvider.createToken(42L, "user@example.com", "USER");

        assertThat(jwtTokenProvider.getMemberId(token)).isEqualTo(42L);
    }

    @Test
    void extractsEmailFromToken() {
        String token = jwtTokenProvider.createToken(1L, "user@example.com", "USER");

        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void extractsRoleFromToken() {
        String token = jwtTokenProvider.createToken(1L, "user@example.com", "ADMIN");

        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void invalidatesGarbageToken() {
        assertThat(jwtTokenProvider.validateToken("garbage")).isFalse();
    }

    @Test
    void invalidatesExpiredToken() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, 0L);
        String token = expiredProvider.createToken(1L, "test@example.com", "USER");

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    void invalidatesTokenWithWrongSecret() {
        String token = jwtTokenProvider.createToken(1L, "test@example.com", "USER");

        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "another-secret-key-that-is-at-least-256-bits-long-for-hs", EXPIRATION
        );
        assertThat(otherProvider.validateToken(token)).isFalse();
    }
}
