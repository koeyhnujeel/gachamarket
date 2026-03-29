package com.gachamarket.category.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gachamarket.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void returnsVisibleLeafSlugsFromDatabase() throws Exception {
        mockMvc.perform(get("/api/categories/leaf-slugs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("baseball-kbo"))
            .andExpect(jsonPath("$[1].slug").value("basketball-nba"))
            .andExpect(jsonPath("$[2].slug").value("football-epl"));

        Integer visibleLeafCount = jdbcTemplate.queryForObject(
            """
                select count(*)
                from categories c
                where visible = true
                  and not exists (
                    select 1
                    from categories child
                    where child.parent_id = c.id
                  )
                """,
            Integer.class
        );

        assertThat(visibleLeafCount).isEqualTo(3);
    }
}
