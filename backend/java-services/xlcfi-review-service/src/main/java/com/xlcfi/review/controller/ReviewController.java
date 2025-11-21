package com.xlcfi.review.controller;

import com.xlcfi.common.dto.ApiResponse;
import com.xlcfi.review.dto.CreateReviewRequest;
import com.xlcfi.review.dto.ReviewResponse;
import com.xlcfi.review.dto.UpdateReviewRequest;
import com.xlcfi.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 리뷰 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateReviewRequest request) {
        
        log.info("리뷰 작성 요청: userId={}, productId={}", userId, request.getProductId());
        
        ReviewResponse review = reviewService.createReview(userId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(review, "리뷰가 작성되었습니다"));
    }

    /**
     * 리뷰 수정
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateReviewRequest request) {
        
        log.info("리뷰 수정 요청: reviewId={}, userId={}", reviewId, userId);
        
        ReviewResponse review = reviewService.updateReview(reviewId, userId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(review, "리뷰가 수정되었습니다"));
    }

    /**
     * 리뷰 삭제
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @RequestAttribute("userId") Long userId) {
        
        log.info("리뷰 삭제 요청: reviewId={}, userId={}", reviewId, userId);
        
        reviewService.deleteReview(reviewId, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "리뷰가 삭제되었습니다"));
    }

    /**
     * 리뷰 상세 조회
     * GET /api/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
            @PathVariable Long reviewId) {
        
        log.info("리뷰 조회 요청: reviewId={}", reviewId);
        
        ReviewResponse review = reviewService.getReview(reviewId);
        
        return ResponseEntity.ok(
                ApiResponse.success(review, "리뷰 조회 성공"));
    }

    /**
     * 상품별 리뷰 목록 조회
     * GET /api/reviews/product/{productId}?page=0&size=20
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("상품별 리뷰 목록 조회 요청: productId={}", productId);
        
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(reviews, "리뷰 목록 조회 성공"));
    }

    /**
     * 내 리뷰 목록 조회
     * GET /api/reviews/my?page=0&size=20
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("내 리뷰 목록 조회 요청: userId={}", userId);
        
        Page<ReviewResponse> reviews = reviewService.getReviewsByUser(userId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(reviews, "리뷰 목록 조회 성공"));
    }

    /**
     * 평점별 리뷰 조회
     * GET /api/reviews/product/{productId}/rating/{rating}?page=0&size=20
     */
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByRating(
            @PathVariable Long productId,
            @PathVariable Integer rating,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("평점별 리뷰 조회 요청: productId={}, rating={}", productId, rating);
        
        Page<ReviewResponse> reviews = reviewService.getReviewsByRating(
                productId, rating, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(reviews, "평점별 리뷰 조회 성공"));
    }

    /**
     * 인증 구매 리뷰만 조회
     * GET /api/reviews/product/{productId}/verified?page=0&size=20
     */
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getVerifiedPurchaseReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("인증 구매 리뷰 조회 요청: productId={}", productId);
        
        Page<ReviewResponse> reviews = reviewService.getVerifiedPurchaseReviews(
                productId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(reviews, "인증 구매 리뷰 조회 성공"));
    }

    /**
     * 최신 리뷰 조회
     * GET /api/reviews/latest?page=0&size=20
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getLatestReviews(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("최신 리뷰 조회 요청");
        
        Page<ReviewResponse> reviews = reviewService.getLatestReviews(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(reviews, "최신 리뷰 조회 성공"));
    }
}

