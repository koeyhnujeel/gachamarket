package com.gachamarket.category.adapter.out.persistence;

import com.gachamarket.category.application.port.out.LoadCategoryPort;
import com.gachamarket.category.domain.Category;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceAdapter implements LoadCategoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryPersistenceAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public List<Category> loadVisibleLeafCategories() {
        return categoryJpaRepository.findVisibleLeafCategories().stream()
            .map(entity -> entity.toDomain(true))
            .toList();
    }
}
