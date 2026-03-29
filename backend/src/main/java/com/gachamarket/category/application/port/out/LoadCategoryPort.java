package com.gachamarket.category.application.port.out;

import com.gachamarket.category.domain.Category;
import java.util.List;

public interface LoadCategoryPort {

    List<Category> loadVisibleLeafCategories();
}
