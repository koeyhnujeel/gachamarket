package com.gachamarket.identity.application;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {

    public String generate() {
        return "GM-" + ThreadLocalRandom.current().nextInt(100000, 1_000_000);
    }
}
