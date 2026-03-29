package com.gachamarket.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@Import(com.gachamarket.TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest
class ApplicationContextSmokeTest {

    @Test
    void contextLoads() {
    }
}
