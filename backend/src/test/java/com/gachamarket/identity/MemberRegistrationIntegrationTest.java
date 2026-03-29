package com.gachamarket.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(com.gachamarket.TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest
class MemberRegistrationIntegrationTest {

    @Autowired
    private com.gachamarket.identity.application.MemberRegistrationService memberRegistrationService;

    @Test
    void createsMemberWithInitialNicknameAndPoints() {
        var member = memberRegistrationService.registerOrLoad("user@example.com");

        assertThat(member.email()).isEqualTo("user@example.com");
        assertThat(member.nickname()).startsWith("GM-");
        assertThat(member.currentPoint()).isEqualTo(1000);
        assertThat(member.id()).isInstanceOf(UUID.class);
    }
}
