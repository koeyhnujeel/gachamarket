package com.gachamarket.category.adapter.out.persistence;

import com.gachamarket.category.application.port.out.LoadCategoryPort;
import com.gachamarket.category.domain.Category;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceAdapter implements LoadCategoryPort {

    private final JpaCategoryRepository jpaCategoryRepository;

    public CategoryPersistenceAdapter(JpaCategoryRepository jpaCategoryRepository) {
        this.jpaCategoryRepository = jpaCategoryRepository;
    }

    @Override
    public List<Category> loadVisibleLeafCategories() {
        return jpaCategoryRepository.findVisibleLeafCategories().stream()
            .map(entity -> entity.toDomain(true))
            .toList();
    }
}
