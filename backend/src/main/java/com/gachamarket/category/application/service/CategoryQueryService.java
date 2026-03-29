package com.gachamarket.category.application.service;

import com.gachamarket.category.application.dto.result.CategoryLeafResult;
import com.gachamarket.category.application.port.in.GetVisibleLeafCategoriesUseCase;
import com.gachamarket.category.application.port.out.LoadCategoryPort;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryService implements GetVisibleLeafCategoriesUseCase {

    private final LoadCategoryPort loadCategoryPort;

    public CategoryQueryService(LoadCategoryPort loadCategoryPort) {
        this.loadCategoryPort = loadCategoryPort;
    }

    @Override
    public List<CategoryLeafResult> getVisibleLeafCategories() {
        return loadCategoryPort.loadVisibleLeafCategories().stream()
            .map(category -> new CategoryLeafResult(category.getSlug()))
            .toList();
    }
}
