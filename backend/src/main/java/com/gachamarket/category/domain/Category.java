package com.gachamarket.category.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {

    private final UUID id;
    private final UUID parentId;
    private final String slug;
    private final String name;
    private final int depth;
    private final int sortOrder;
    private final boolean visible;
    private final boolean leaf;

    public static Category of(
        UUID id,
        UUID parentId,
        String slug,
        String name,
        int depth,
        int sortOrder,
        boolean visible,
        boolean leaf
    ) {
        return new Category(id, parentId, slug, name, depth, sortOrder, visible, leaf);
    }

    public boolean isVisibleLeaf() {
        return visible && leaf;
    }
}
