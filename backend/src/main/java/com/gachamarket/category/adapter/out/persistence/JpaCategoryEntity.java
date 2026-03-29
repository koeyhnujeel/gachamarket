package com.gachamarket.category.adapter.out.persistence;

import com.gachamarket.category.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class JpaCategoryEntity {

    @Id
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false)
    private int depth;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean visible;

    protected JpaCategoryEntity() {
    }

    public Category toDomain(boolean leaf) {
        return new Category(id, parentId, slug, name, depth, sortOrder, visible, leaf);
    }
}
