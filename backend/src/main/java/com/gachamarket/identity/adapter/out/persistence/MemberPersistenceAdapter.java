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

    private final JpaMemberRepository jpaMemberRepository;
    private final JpaWalletRepository jpaWalletRepository;

    public MemberPersistenceAdapter(
        JpaMemberRepository jpaMemberRepository,
        JpaWalletRepository jpaWalletRepository
    ) {
        this.jpaMemberRepository = jpaMemberRepository;
        this.jpaWalletRepository = jpaWalletRepository;
    }

    @Override
    public Optional<MemberAccount> loadByEmail(String email) {
        return jpaMemberRepository.findByEmail(email)
            .map(memberEntity -> {
                Wallet wallet = jpaWalletRepository.findById(memberEntity.getId())
                    .map(JpaWalletEntity::toDomain)
                    .orElseThrow(() -> new IllegalStateException("wallet not found for member " + memberEntity.getId()));

                return new MemberAccount(memberEntity.toDomain(), wallet);
            });
    }

    @Override
    public void save(Member member, Wallet wallet, Instant now) {
        jpaMemberRepository.save(JpaMemberEntity.from(member, now));
        jpaWalletRepository.save(JpaWalletEntity.from(wallet, now));
    }
}
