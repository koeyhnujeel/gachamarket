package com.gachamarket.category.application.port.in;

import com.gachamarket.category.application.dto.CategoryLeafDto;
import java.util.List;

public interface GetVisibleLeafCategoriesUseCase {

    List<CategoryLeafDto> getVisibleLeafCategories();
}
