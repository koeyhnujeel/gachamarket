package com.gachamarket.category.application.port.in;

import com.gachamarket.category.application.dto.result.CategoryLeafResult;
import java.util.List;

public interface GetVisibleLeafCategoriesUseCase {

    List<CategoryLeafResult> getVisibleLeafCategories();
}
