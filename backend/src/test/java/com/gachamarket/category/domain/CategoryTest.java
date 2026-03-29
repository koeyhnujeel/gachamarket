package com.gachamarket.category.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void identifiesVisibleLeafCategory() {
        Category category = new Category(
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            UUID.fromString("00000000-0000-0000-0000-000000000011"),
            "football-epl",
            "EPL",
            2,
            0,
            true,
            true
        );

        assertThat(category.isVisibleLeaf()).isTrue();
    }
}
