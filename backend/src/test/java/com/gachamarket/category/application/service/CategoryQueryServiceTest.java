package com.gachamarket.category.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gachamarket.category.application.dto.CategoryLeafDto;
import com.gachamarket.category.application.port.out.LoadCategoryPort;
import com.gachamarket.category.domain.Category;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryQueryServiceTest {

    @Test
    void returnsVisibleLeafCategoryDtosFromPort() {
        LoadCategoryPort loadCategoryPort = mock(LoadCategoryPort.class);
        CategoryQueryService categoryQueryService = new CategoryQueryService(loadCategoryPort);

        when(loadCategoryPort.loadVisibleLeafCategories()).thenReturn(List.of(
            new Category(
                UUID.fromString("00000000-0000-0000-0000-000000000021"),
                UUID.fromString("00000000-0000-0000-0000-000000000011"),
                "football-epl",
                "EPL",
                2,
                0,
                true,
                true
            ),
            new Category(
                UUID.fromString("00000000-0000-0000-0000-000000000022"),
                UUID.fromString("00000000-0000-0000-0000-000000000012"),
                "baseball-kbo",
                "KBO",
                2,
                1,
                true,
                true
            )
        ));

        List<CategoryLeafDto> result = categoryQueryService.getVisibleLeafCategories();

        assertThat(result)
            .extracting(CategoryLeafDto::slug)
            .containsExactly("football-epl", "baseball-kbo");
        verify(loadCategoryPort).loadVisibleLeafCategories();
    }
}
