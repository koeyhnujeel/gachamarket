package com.gachamarket.security.domain;

import com.gachamarket.shared.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserTest {

    @Test
    void createsAuthUserWithAllFields() {
        AuthUser authUser = new AuthUser(1L, "test@example.com", Role.USER);

        assertThat(authUser.memberId()).isEqualTo(1L);
        assertThat(authUser.email()).isEqualTo("test@example.com");
        assertThat(authUser.role()).isEqualTo(Role.USER);
    }

    @Test
    void createsAdminAuthUser() {
        AuthUser authUser = new AuthUser(2L, "admin@example.com", Role.ADMIN);

        assertThat(authUser.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void equalityBasedOnValue() {
        AuthUser user1 = new AuthUser(1L, "test@example.com", Role.USER);
        AuthUser user2 = new AuthUser(1L, "test@example.com", Role.USER);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
}
