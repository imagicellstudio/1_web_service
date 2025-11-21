package com.xlcfi.review.repository;

import com.xlcfi.review.domain.Review;
import com.xlcfi.review.domain.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 상품별 리뷰 조회 (페이징)
     */
    Page<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);
    
    /**
     * 사용자별 리뷰 조회 (페이징)
     */
    Page<Review> findByUserIdAndStatus(Long userId, ReviewStatus status, Pageable pageable);
    
    /**
     * 상품 + 사용자로 리뷰 조회 (중복 확인용)
     */
    Optional<Review> findByProductIdAndUserIdAndStatus(Long productId, Long userId, ReviewStatus status);
    
    /**
     * 상품별 평균 평점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    Double getAverageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);
    
    /**
     * 상품별 리뷰 개수 조회
     */
    Long countByProductIdAndStatus(Long productId, ReviewStatus status);
    
    /**
     * 평점별 리뷰 조회
     */
    Page<Review> findByProductIdAndRatingAndStatus(Long productId, Integer rating, ReviewStatus status, Pageable pageable);
    
    /**
     * 인증된 구매 리뷰만 조회
     */
    Page<Review> findByProductIdAndIsVerifiedPurchaseAndStatus(Long productId, Boolean isVerifiedPurchase, ReviewStatus status, Pageable pageable);
    
    /**
     * 최신 리뷰 조회
     */
    Page<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);
}

