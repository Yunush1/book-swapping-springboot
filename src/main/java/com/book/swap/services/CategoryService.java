package com.book.swap.services;

import com.book.swap.models.dto.CategoryDTO;
import com.book.swap.models.entities.DbCategory;
import com.book.swap.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public CategoryDTO createCategory(CategoryDTO categoryDTO){
            return null;
    }

    public List<DbCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
}
