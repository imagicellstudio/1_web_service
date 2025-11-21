package com.xlcfi.product.controller;

import com.xlcfi.common.dto.ApiResponse;
import com.xlcfi.product.dto.CategoryResponse;
import com.xlcfi.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 전체 카테고리 조회 (계층 구조)
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        log.info("전체 카테고리 조회 요청");
        
        List<CategoryResponse> categories = categoryService.getAllCategories();
        
        return ResponseEntity.ok(
                ApiResponse.success(categories, "카테고리 조회 성공"));
    }

    /**
     * 특정 카테고리 조회
     * GET /api/categories/{categoryId}
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable Long categoryId) {
        
        log.info("카테고리 조회 요청: categoryId={}", categoryId);
        
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        
        return ResponseEntity.ok(
                ApiResponse.success(category, "카테고리 조회 성공"));
    }

    /**
     * 자식 카테고리 조회
     * GET /api/categories/{parentId}/children
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(
            @PathVariable Long parentId) {
        
        log.info("자식 카테고리 조회 요청: parentId={}", parentId);
        
        List<CategoryResponse> children = categoryService.getChildCategories(parentId);
        
        return ResponseEntity.ok(
                ApiResponse.success(children, "자식 카테고리 조회 성공"));
    }

    /**
     * 카테고리 검색
     * GET /api/categories/search?keyword={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(
            @RequestParam String keyword) {
        
        log.info("카테고리 검색 요청: keyword={}", keyword);
        
        List<CategoryResponse> categories = categoryService.searchCategories(keyword);
        
        return ResponseEntity.ok(
                ApiResponse.success(categories, "카테고리 검색 성공"));
    }
}

