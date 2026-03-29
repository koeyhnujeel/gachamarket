package com.gachamarket.category.adapter.in.web;

import com.gachamarket.category.application.CategoryQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    public CategoryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping("/leaf-slugs")
    public List<String> leafSlugs() {
        return categoryQueryService.findVisibleLeafSlugs();
    }
}
