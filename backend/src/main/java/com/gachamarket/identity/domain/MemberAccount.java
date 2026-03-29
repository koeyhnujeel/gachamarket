package com.gachamarket.identity.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberAccount {

    private final Member member;
    private final Wallet wallet;

    public static MemberAccount of(Member member, Wallet wallet) {
        return new MemberAccount(member, wallet);
    }
}
