package com.onlinestore.productservice.service;

import com.onlinestore.productservice.dto.CategoryDTO;
import com.onlinestore.productservice.dto.CreateCategoryRequest;
import com.onlinestore.productservice.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(Long id);
    CategoryDTO createCategory(CreateCategoryRequest request);
    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);
    void deleteCategory(Long id);
}
