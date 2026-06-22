package ewm.categories.service;

import ewm.categories.dto.CategoryDto;
import ewm.categories.dto.NewCategoryDto;
import org.springframework.data.domain.Page;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    void deleteCategory(Long catId);

    Page<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);
}