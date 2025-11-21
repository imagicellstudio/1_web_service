package com.xlcfi.product.repository;

import com.xlcfi.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 최상위 카테고리 조회 (parent_id가 null인 카테고리)
     */
    List<Category> findByParentIsNull();
    
    /**
     * 특정 부모 카테고리의 자식 카테고리 조회
     */
    List<Category> findByParentIdOrderBySortOrder(Long parentId);
    
    /**
     * 카테고리 이름으로 검색
     */
    List<Category> findByNameContaining(String name);
    
    /**
     * 모든 카테고리를 정렬 순서대로 조회
     */
    List<Category> findAllByOrderBySortOrder();
}

