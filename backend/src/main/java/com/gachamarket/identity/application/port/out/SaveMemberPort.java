package com.gachamarket.identity.application.port.out;

import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.Wallet;
import java.time.Instant;

public interface SaveMemberPort {

    void save(Member member, Wallet wallet, Instant now);
}
