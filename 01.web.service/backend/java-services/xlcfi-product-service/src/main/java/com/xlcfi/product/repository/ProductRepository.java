package com.xlcfi.product.repository;

import com.xlcfi.product.domain.Product;
import com.xlcfi.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 상태별 상품 조회 (페이징)
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * 카테고리별 상품 조회 (페이징)
     */
    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status, Pageable pageable);
    
    /**
     * 판매자별 상품 조회 (페이징)
     */
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    
    /**
     * 상품명 검색 (한글/영문, 페이징)
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(p.name LIKE %:keyword% OR p.nameEn LIKE %:keyword%) " +
           "AND p.status = :status")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, 
                                   @Param("status") ProductStatus status, 
                                   Pageable pageable);
    
    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);
    
    /**
     * 인기 상품 조회 (조회수 기준, 페이징)
     */
    Page<Product> findByStatusOrderByViewCountDesc(ProductStatus status, Pageable pageable);
    
    /**
     * 평점 높은 상품 조회 (페이징)
     */
    Page<Product> findByStatusOrderByRatingAverageDesc(ProductStatus status, Pageable pageable);
    
    /**
     * 최신 상품 조회 (페이징)
     */
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
}

