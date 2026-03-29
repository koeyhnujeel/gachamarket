package com.gachamarket.category.application;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryService {

    private final JdbcTemplate jdbcTemplate;

    public CategoryQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> findVisibleLeafSlugs() {
        return jdbcTemplate.queryForList(
            """
                select slug
                from categories c
                where visible = true
                  and not exists (
                    select 1
                    from categories child
                    where child.parent_id = c.id
                  )
                order by sort_order, slug
                """,
            String.class
        );
    }
}
