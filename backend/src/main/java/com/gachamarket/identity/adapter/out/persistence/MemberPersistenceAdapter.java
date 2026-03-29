package com.gachamarket.identity.adapter.out.persistence;

import com.gachamarket.identity.application.port.out.LoadMemberPort;
import com.gachamarket.identity.application.port.out.SaveMemberPort;
import com.gachamarket.identity.domain.Member;
import com.gachamarket.identity.domain.MemberAccount;
import com.gachamarket.identity.domain.Wallet;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MemberPersistenceAdapter implements LoadMemberPort, SaveMemberPort {

    private final MemberJpaRepository memberJpaRepository;
    private final WalletJpaRepository walletJpaRepository;

    public MemberPersistenceAdapter(
        MemberJpaRepository memberJpaRepository,
        WalletJpaRepository walletJpaRepository
    ) {
        this.memberJpaRepository = memberJpaRepository;
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public Optional<MemberAccount> loadByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
            .map(memberEntity -> {
                Wallet wallet = walletJpaRepository.findById(memberEntity.getId())
                    .map(WalletJpaEntity::toDomain)
                    .orElseThrow(() -> new IllegalStateException("wallet not found for member " + memberEntity.getId()));

                return MemberAccount.of(memberEntity.toDomain(), wallet);
            });
    }

    @Override
    public void save(Member member, Wallet wallet, Instant now) {
        memberJpaRepository.save(MemberJpaEntity.from(member, now));
        walletJpaRepository.save(WalletJpaEntity.from(wallet, now));
    }
}
