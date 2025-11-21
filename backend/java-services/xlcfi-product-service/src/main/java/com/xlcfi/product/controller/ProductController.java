package com.xlcfi.product.controller;

import com.xlcfi.common.dto.ApiResponse;
import com.xlcfi.product.domain.ProductStatus;
import com.xlcfi.product.dto.ProductRequest;
import com.xlcfi.product.dto.ProductResponse;
import com.xlcfi.product.service.ProductService;
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
 * 상품 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestAttribute("userId") Long sellerId,
            @Valid @RequestBody ProductRequest request) {
        
        log.info("상품 등록 요청: sellerId={}", sellerId);
        
        ProductResponse product = productService.createProduct(sellerId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "상품이 등록되었습니다"));
    }

    /**
     * 상품 수정
     * PUT /api/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @RequestAttribute("userId") Long sellerId,
            @Valid @RequestBody ProductRequest request) {
        
        log.info("상품 수정 요청: productId={}, sellerId={}", productId, sellerId);
        
        ProductResponse product = productService.updateProduct(productId, sellerId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(product, "상품이 수정되었습니다"));
    }

    /**
     * 상품 삭제
     * DELETE /api/products/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            @RequestAttribute("userId") Long sellerId) {
        
        log.info("상품 삭제 요청: productId={}, sellerId={}", productId, sellerId);
        
        productService.deleteProduct(productId, sellerId);
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "상품이 삭제되었습니다"));
    }

    /**
     * 상품 상태 변경
     * PATCH /api/products/{productId}/status
     */
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable Long productId,
            @RequestAttribute("userId") Long sellerId,
            @RequestParam ProductStatus status) {
        
        log.info("상품 상태 변경 요청: productId={}, status={}", productId, status);
        
        ProductResponse product = productService.updateProductStatus(productId, sellerId, status);
        
        return ResponseEntity.ok(
                ApiResponse.success(product, "상품 상태가 변경되었습니다"));
    }

    /**
     * 상품 상세 조회
     * GET /api/products/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long productId) {
        
        log.info("상품 조회 요청: productId={}", productId);
        
        ProductResponse product = productService.getProduct(productId);
        
        return ResponseEntity.ok(
                ApiResponse.success(product, "상품 조회 성공"));
    }

    /**
     * 상품 목록 조회 (전체)
     * GET /api/products?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("상품 목록 조회 요청: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ProductResponse> products = productService.getProducts(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "상품 목록 조회 성공"));
    }

    /**
     * 카테고리별 상품 조회
     * GET /api/products/category/{categoryId}?page=0&size=20
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("카테고리별 상품 조회 요청: categoryId={}", categoryId);
        
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "카테고리별 상품 조회 성공"));
    }

    /**
     * 판매자별 상품 조회
     * GET /api/products/seller/{sellerId}?page=0&size=20
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("판매자별 상품 조회 요청: sellerId={}", sellerId);
        
        Page<ProductResponse> products = productService.getProductsBySeller(sellerId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "판매자별 상품 조회 성공"));
    }

    /**
     * 상품 검색
     * GET /api/products/search?keyword={keyword}&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("상품 검색 요청: keyword={}", keyword);
        
        Page<ProductResponse> products = productService.searchProducts(keyword, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "상품 검색 성공"));
    }

    /**
     * 인기 상품 조회 (조회수 기준)
     * GET /api/products/popular?page=0&size=20
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getPopularProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("인기 상품 조회 요청");
        
        Page<ProductResponse> products = productService.getPopularProducts(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "인기 상품 조회 성공"));
    }

    /**
     * 평점 높은 상품 조회
     * GET /api/products/top-rated?page=0&size=20
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getTopRatedProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("평점 높은 상품 조회 요청");
        
        Page<ProductResponse> products = productService.getTopRatedProducts(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "평점 높은 상품 조회 성공"));
    }

    /**
     * 최신 상품 조회
     * GET /api/products/latest?page=0&size=20
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getLatestProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("최신 상품 조회 요청");
        
        Page<ProductResponse> products = productService.getLatestProducts(pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(products, "최신 상품 조회 성공"));
    }
}

