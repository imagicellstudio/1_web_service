package com.xlcfi.product.service;

import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.product.domain.Category;
import com.xlcfi.product.dto.CategoryResponse;
import com.xlcfi.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 모든 카테고리 조회 (계층 구조)
     */
    public List<CategoryResponse> getAllCategories() {
        log.info("전체 카테고리 조회");
        
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        
        return rootCategories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리 조회
     */
    public CategoryResponse getCategoryById(Long categoryId) {
        log.info("카테고리 조회: categoryId={}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException("CATEGORY001", "카테고리를 찾을 수 없습니다"));
        
        return CategoryResponse.from(category);
    }

    /**
     * 자식 카테고리 조회
     */
    public List<CategoryResponse> getChildCategories(Long parentId) {
        log.info("자식 카테고리 조회: parentId={}", parentId);
        
        // 부모 카테고리 존재 확인
        if (!categoryRepository.existsById(parentId)) {
            throw new BusinessException("CATEGORY001", "부모 카테고리를 찾을 수 없습니다");
        }
        
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(parentId);
        
        return children.stream()
                .map(CategoryResponse::fromWithoutChildren)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 검색
     */
    public List<CategoryResponse> searchCategories(String keyword) {
        log.info("카테고리 검색: keyword={}", keyword);
        
        List<Category> categories = categoryRepository.findByNameContaining(keyword);
        
        return categories.stream()
                .map(CategoryResponse::fromWithoutChildren)
                .collect(Collectors.toList());
    }
}

