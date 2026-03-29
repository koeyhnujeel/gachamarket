package com.gachamarket.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(com.gachamarket.TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationContextSmokeTest {

    @Test
    void contextLoads() {
    }
}
