package com.gachamarket.category.domain;

import java.util.UUID;

public record Category(
    UUID id,
    UUID parentId,
    String slug,
    String name,
    int depth,
    int sortOrder,
    boolean visible,
    boolean leaf
) {

    public boolean isVisibleLeaf() {
        return visible && leaf;
    }
}
