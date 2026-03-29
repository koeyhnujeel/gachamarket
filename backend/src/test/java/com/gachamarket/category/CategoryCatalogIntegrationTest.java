package com.gachamarket.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(com.gachamarket.TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest
class CategoryCatalogIntegrationTest {

    @Autowired
    private com.gachamarket.category.application.CategoryQueryService categoryQueryService;

    @Test
    void returnsSeededSportsTree() {
        List<String> slugs = categoryQueryService.findVisibleLeafSlugs();
        assertThat(slugs).contains("football-epl", "baseball-kbo", "basketball-nba");
    }
}
