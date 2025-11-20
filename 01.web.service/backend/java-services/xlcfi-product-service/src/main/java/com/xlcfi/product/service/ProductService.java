package com.xlcfi.product.service;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.repository.UserRepository;
import com.xlcfi.common.exception.BusinessException;
import com.xlcfi.product.domain.Category;
import com.xlcfi.product.domain.Product;
import com.xlcfi.product.domain.ProductStatus;
import com.xlcfi.product.dto.ProductRequest;
import com.xlcfi.product.dto.ProductResponse;
import com.xlcfi.product.repository.CategoryRepository;
import com.xlcfi.product.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 상품 등록
     */
    @Transactional
    public ProductResponse createProduct(Long sellerId, ProductRequest request) {
        log.info("상품 등록: sellerId={}, productName={}", sellerId, request.getName());

        // 판매자 조회
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException("PRODUCT001", "판매자를 찾을 수 없습니다"));

        // 카테고리 조회 (선택사항)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("PRODUCT002", "카테고리를 찾을 수 없습니다"));
        }

        // 상품 생성
        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(request.getName())
                .nameEn(request.getNameEn())
                .description(request.getDescription())
                .descriptionEn(request.getDescriptionEn())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .stockQuantity(request.getStockQuantity())
                .images(request.getImages())
                .status(ProductStatus.DRAFT)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("상품 등록 완료: productId={}", savedProduct.getId());

        return ProductResponse.from(savedProduct);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, Long sellerId, ProductRequest request) {
        log.info("상품 수정: productId={}, sellerId={}", productId, sellerId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT003", "상품을 찾을 수 없습니다"));

        // 판매자 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("PRODUCT004", "상품을 수정할 권한이 없습니다");
        }

        // 카테고리 변경
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException("PRODUCT002", "카테고리를 찾을 수 없습니다"));
            product.setCategory(category);
        }

        // 상품 정보 업데이트
        product.setName(request.getName());
        product.setNameEn(request.getNameEn());
        product.setDescription(request.getDescription());
        product.setDescriptionEn(request.getDescriptionEn());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency());
        product.setStockQuantity(request.getStockQuantity());
        product.setImages(request.getImages());

        Product updatedProduct = productRepository.save(product);
        log.info("상품 수정 완료: productId={}", updatedProduct.getId());

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        log.info("상품 삭제: productId={}, sellerId={}", productId, sellerId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT003", "상품을 찾을 수 없습니다"));

        // 판매자 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("PRODUCT005", "상품을 삭제할 권한이 없습니다");
        }

        productRepository.delete(product);
        log.info("상품 삭제 완료: productId={}", productId);
    }

    /**
     * 상품 상태 변경
     */
    @Transactional
    public ProductResponse updateProductStatus(Long productId, Long sellerId, ProductStatus status) {
        log.info("상품 상태 변경: productId={}, status={}", productId, status);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT003", "상품을 찾을 수 없습니다"));

        // 판매자 권한 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("PRODUCT006", "상품 상태를 변경할 권한이 없습니다");
        }

        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        log.info("상품 상태 변경 완료: productId={}, status={}", productId, status);

        return ProductResponse.from(updatedProduct);
    }

    /**
     * 상품 상세 조회
     */
    @Transactional
    public ProductResponse getProduct(Long productId) {
        log.info("상품 조회: productId={}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("PRODUCT003", "상품을 찾을 수 없습니다"));

        // 조회수 증가
        productRepository.incrementViewCount(productId);

        return ProductResponse.from(product);
    }

    /**
     * 상품 목록 조회 (전체)
     */
    public Page<ProductResponse> getProducts(Pageable pageable) {
        log.info("상품 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findByStatus(ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 카테고리별 상품 조회
     */
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("카테고리별 상품 조회: categoryId={}", categoryId);

        Page<Product> products = productRepository.findByCategoryIdAndStatus(
                categoryId, ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 판매자별 상품 조회
     */
    public Page<ProductResponse> getProductsBySeller(Long sellerId, Pageable pageable) {
        log.info("판매자별 상품 조회: sellerId={}", sellerId);

        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 상품 검색
     */
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        log.info("상품 검색: keyword={}", keyword);

        Page<Product> products = productRepository.searchByKeyword(
                keyword, ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 인기 상품 조회
     */
    public Page<ProductResponse> getPopularProducts(Pageable pageable) {
        log.info("인기 상품 조회");

        Page<Product> products = productRepository.findByStatusOrderByViewCountDesc(
                ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 평점 높은 상품 조회
     */
    public Page<ProductResponse> getTopRatedProducts(Pageable pageable) {
        log.info("평점 높은 상품 조회");

        Page<Product> products = productRepository.findByStatusOrderByRatingAverageDesc(
                ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }

    /**
     * 최신 상품 조회
     */
    public Page<ProductResponse> getLatestProducts(Pageable pageable) {
        log.info("최신 상품 조회");

        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(
                ProductStatus.PUBLISHED, pageable);

        return products.map(ProductResponse::from);
    }
}

