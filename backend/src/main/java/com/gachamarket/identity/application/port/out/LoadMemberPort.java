package com.gachamarket.identity.application.port.out;

import com.gachamarket.identity.domain.MemberAccount;
import java.util.Optional;

public interface LoadMemberPort {

    Optional<MemberAccount> loadByEmail(String email);
}
