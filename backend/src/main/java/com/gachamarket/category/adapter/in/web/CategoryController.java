package com.gachamarket.category.adapter.in.web;

import com.gachamarket.category.adapter.in.web.response.CategoryLeafResponse;
import com.gachamarket.category.application.port.in.GetVisibleLeafCategoriesUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final GetVisibleLeafCategoriesUseCase getVisibleLeafCategoriesUseCase;

    public CategoryController(GetVisibleLeafCategoriesUseCase getVisibleLeafCategoriesUseCase) {
        this.getVisibleLeafCategoriesUseCase = getVisibleLeafCategoriesUseCase;
    }

    @GetMapping("/leaf-slugs")
    public List<CategoryLeafResponse> leafSlugs() {
        return getVisibleLeafCategoriesUseCase.getVisibleLeafCategories().stream()
            .map(category -> new CategoryLeafResponse(category.slug()))
            .toList();
    }
}
