package com.xlcfi.review.service;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.repository.UserRepository;
import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.product.domain.Product;
import com.xlcfi.product.repository.ProductRepository;
import com.xlcfi.review.domain.Review;
import com.xlcfi.review.domain.ReviewStatus;
import com.xlcfi.review.dto.CreateReviewRequest;
import com.xlcfi.review.dto.ReviewResponse;
import com.xlcfi.review.dto.UpdateReviewRequest;
import com.xlcfi.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 리뷰 작성
     */
    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        log.info("리뷰 작성: userId={}, productId={}", userId, request.getProductId());

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("REVIEW001", "사용자를 찾을 수 없습니다"));

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("REVIEW002", "상품을 찾을 수 없습니다"));

        // 중복 리뷰 확인
        if (reviewRepository.findByProductIdAndUserIdAndStatus(
                request.getProductId(), userId, ReviewStatus.PUBLISHED).isPresent()) {
            throw new BusinessException("REVIEW003", "이미 리뷰를 작성한 상품입니다");
        }

        // TODO: 실제 구매 여부 확인 (Order 테이블 조회)
        // 현재는 항상 false로 설정
        boolean isVerifiedPurchase = false;

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .isVerifiedPurchase(isVerifiedPurchase)
                .status(ReviewStatus.PUBLISHED)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 작성 완료: reviewId={}", savedReview.getId());

        return ReviewResponse.from(savedReview);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, UpdateReviewRequest request) {
        log.info("리뷰 수정: reviewId={}, userId={}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("REVIEW004", "리뷰를 찾을 수 없습니다"));

        // 작성자 권한 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("REVIEW005", "리뷰를 수정할 권한이 없습니다");
        }

        // 리뷰 수정
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }

        Review updatedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료: reviewId={}", reviewId);

        return ReviewResponse.from(updatedReview);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("리뷰 삭제: reviewId={}, userId={}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("REVIEW004", "리뷰를 찾을 수 없습니다"));

        // 작성자 권한 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("REVIEW006", "리뷰를 삭제할 권한이 없습니다");
        }

        // 소프트 삭제 (상태를 DELETED로 변경)
        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);

        log.info("리뷰 삭제 완료: reviewId={}", reviewId);
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponse getReview(Long reviewId) {
        log.info("리뷰 조회: reviewId={}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("REVIEW004", "리뷰를 찾을 수 없습니다"));

        return ReviewResponse.from(review);
    }

    /**
     * 상품별 리뷰 목록 조회
     */
    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        log.info("상품별 리뷰 목록 조회: productId={}", productId);

        Page<Review> reviews = reviewRepository.findByProductIdAndStatus(
                productId, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 사용자별 리뷰 목록 조회
     */
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        log.info("사용자별 리뷰 목록 조회: userId={}", userId);

        Page<Review> reviews = reviewRepository.findByUserIdAndStatus(
                userId, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 평점별 리뷰 조회
     */
    public Page<ReviewResponse> getReviewsByRating(Long productId, Integer rating, Pageable pageable) {
        log.info("평점별 리뷰 조회: productId={}, rating={}", productId, rating);

        Page<Review> reviews = reviewRepository.findByProductIdAndRatingAndStatus(
                productId, rating, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 인증 구매 리뷰만 조회
     */
    public Page<ReviewResponse> getVerifiedPurchaseReviews(Long productId, Pageable pageable) {
        log.info("인증 구매 리뷰 조회: productId={}", productId);

        Page<Review> reviews = reviewRepository.findByProductIdAndIsVerifiedPurchaseAndStatus(
                productId, true, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(ReviewResponse::from);
    }

    /**
     * 최신 리뷰 조회
     */
    public Page<ReviewResponse> getLatestReviews(Pageable pageable) {
        log.info("최신 리뷰 조회");

        Page<Review> reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(
                ReviewStatus.PUBLISHED, pageable);

        return reviews.map(ReviewResponse::from);
    }
}

