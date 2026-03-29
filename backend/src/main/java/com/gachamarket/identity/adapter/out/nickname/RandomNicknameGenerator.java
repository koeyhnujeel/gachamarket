package com.gachamarket.identity.adapter.out.nickname;

import com.gachamarket.identity.application.port.out.GenerateNicknamePort;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomNicknameGenerator implements GenerateNicknamePort {

    @Override
    public String generate() {
        return "GM-" + ThreadLocalRandom.current().nextInt(100000, 1_000_000);
    }
}
