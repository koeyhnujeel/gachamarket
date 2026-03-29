package com.gachamarket.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletTest {

    @Test
    void opensWalletWithInitialPoints() {
        Wallet wallet = Wallet.open(
            UUID.fromString("10000000-0000-0000-0000-000000000001"),
            1000
        );

        assertThat(wallet.getMemberId()).isEqualTo(UUID.fromString("10000000-0000-0000-0000-000000000001"));
        assertThat(wallet.getCurrentPoint()).isEqualTo(1000);
    }
}
