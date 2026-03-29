package com.gachamarket.category.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, java.util.UUID> {

    @Query("""
        select c
        from CategoryJpaEntity c
        where c.visible = true
          and not exists (
            select 1
            from CategoryJpaEntity child
            where child.parentId = c.id
          )
        order by c.sortOrder, c.slug
        """)
    List<CategoryJpaEntity> findVisibleLeafCategories();
}
