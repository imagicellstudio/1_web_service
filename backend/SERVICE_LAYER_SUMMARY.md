# XLCfi Platform - Service Layer Implementation Summary

## 개요

이 문서는 XLCfi 플랫폼의 Service Layer 구현 내역을 요약합니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. DTO (Data Transfer Object) 클래스

모든 서비스에 대한 Request/Response DTO가 생성되었습니다.

#### Auth Service DTOs
| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| RegisterRequest | 회원가입 요청 | email, password, name, phone, role, language |
| LoginRequest | 로그인 요청 | email, password |
| LoginResponse | 로그인 응답 | accessToken, refreshToken, tokenType, expiresIn, user |
| UserResponse | 사용자 정보 응답 | id, email, name, phone, role, status, language |
| UpdateProfileRequest | 프로필 수정 요청 | name, phone, language |

#### Product Service DTOs
| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| CategoryResponse | 카테고리 응답 | id, parentId, name, nameEn, children |
| ProductRequest | 상품 등록/수정 요청 | name, description, price, currency, stockQuantity, images |
| ProductResponse | 상품 응답 | id, sellerId, categoryId, name, price, status, rating |

#### Order Service DTOs
| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| CreateOrderRequest | 주문 생성 요청 | sellerId, items, shippingAddress |
| OrderItemRequest | 주문 항목 요청 | productId, quantity |
| OrderResponse | 주문 응답 | id, orderNumber, buyerId, sellerId, total, status, items |
| OrderItemResponse | 주문 항목 응답 | id, productId, productName, quantity, unitPrice, subtotal |

#### Payment Service DTOs
| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| CreatePaymentRequest | 결제 생성 요청 | orderId, amount, currency, paymentMethod |
| PaymentResponse | 결제 응답 | id, orderId, amount, status, pgProvider, pgTransactionId |

#### Review Service DTOs
| DTO | 용도 | 주요 필드 |
|-----|------|----------|
| CreateReviewRequest | 리뷰 작성 요청 | productId, rating, title, content, images |
| UpdateReviewRequest | 리뷰 수정 요청 | rating, title, content, images |
| ReviewResponse | 리뷰 응답 | id, productId, userId, rating, content, isVerifiedPurchase |

**특징:**
- Jakarta Validation 어노테이션 활용 (@NotBlank, @Email, @Pattern 등)
- Lombok 활용으로 보일러플레이트 코드 최소화
- Entity → DTO 변환 메서드 (from) 제공

### 2. Service 클래스

모든 비즈니스 로직을 담당하는 Service 클래스가 구현되었습니다.

#### Auth Service

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/service/AuthService.java`

**주요 기능:**
- ✅ 회원가입 (register)
  - 이메일 중복 확인
  - 비밀번호 암호화 (BCrypt)
  - 사용자 생성 및 저장

- ✅ 로그인 (login)
  - 이메일/비밀번호 검증
  - 계정 상태 확인
  - JWT 토큰 생성 (Access + Refresh)
  - 마지막 로그인 시간 업데이트

- ✅ 프로필 조회 (getProfile)
- ✅ 프로필 수정 (updateProfile)
- ✅ 토큰 갱신 (refreshToken)

**의존성:**
- UserRepository
- PasswordEncoder (BCrypt)
- JwtTokenProvider

#### JWT Token Provider

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/service/JwtTokenProvider.java`

**주요 기능:**
- Access Token 생성 (1시간 유효)
- Refresh Token 생성 (30일 유효)
- 토큰 검증
- 토큰에서 사용자 정보 추출 (userId, email, role)

**사용 라이브러리:** io.jsonwebtoken (JJWT) 0.12.3

#### Product Service

**파일:** 
- `xlcfi-product-service/src/main/java/com/xlcfi/product/service/CategoryService.java`
- `xlcfi-product-service/src/main/java/com/xlcfi/product/service/ProductService.java`

**CategoryService 주요 기능:**
- ✅ 전체 카테고리 조회 (계층 구조)
- ✅ 특정 카테고리 조회
- ✅ 자식 카테고리 조회
- ✅ 카테고리 검색

**ProductService 주요 기능:**
- ✅ 상품 등록 (createProduct)
  - 판매자/카테고리 검증
  - 초기 상태: DRAFT

- ✅ 상품 수정 (updateProduct)
  - 판매자 권한 확인
  - 상품 정보 업데이트

- ✅ 상품 삭제 (deleteProduct)
- ✅ 상품 상태 변경 (updateProductStatus)
- ✅ 상품 상세 조회 (getProduct)
  - 조회수 자동 증가

- ✅ 상품 목록 조회
  - 전체 상품 (getProducts)
  - 카테고리별 (getProductsByCategory)
  - 판매자별 (getProductsBySeller)
  - 검색 (searchProducts)
  - 인기 상품 (getPopularProducts) - 조회수 기준
  - 평점 높은 상품 (getTopRatedProducts)
  - 최신 상품 (getLatestProducts)

**의존성:**
- ProductRepository
- CategoryRepository
- UserRepository

#### Order Service

**파일:** `xlcfi-order-service/src/main/java/com/xlcfi/order/service/OrderService.java`

**주요 기능:**
- ✅ 주문 생성 (createOrder)
  - 구매자/판매자 검증
  - 재고 확인
  - 주문 항목 생성
  - 총액 계산
  - 주문번호 자동 생성 (트리거)

- ✅ 주문 조회 (getOrder)
  - 권한 확인 (구매자 또는 판매자)

- ✅ 주문 목록 조회
  - 구매자별 (getOrdersByBuyer)
  - 판매자별 (getOrdersBySeller)

- ✅ 주문 상태 변경 (updateOrderStatus)
  - 판매자만 가능
  - 상태 전환 규칙 검증

- ✅ 주문 취소 (cancelOrder)
  - 구매자만 가능
  - 취소 가능 상태 확인

**상태 전환 규칙:**
```
PENDING → PAID → CONFIRMED → SHIPPING → DELIVERED
   ↓        ↓
CANCELLED  CANCELLED
```

**의존성:**
- OrderRepository
- UserRepository
- ProductRepository

#### Payment Service

**파일:** `xlcfi-payment-service/src/main/java/com/xlcfi/payment/service/PaymentService.java`

**주요 기능:**
- ✅ 결제 생성 (createPayment)
  - 주문 조회
  - 결제 금액 검증

- ✅ 결제 처리 (processPayment)
  - PG사 API 연동 (시뮬레이션)
  - 결제 상태 업데이트
  - PG사 응답 저장

- ✅ 결제 조회 (getPayment)
- ✅ 주문별 결제 목록 조회 (getPaymentsByOrder)
- ✅ 결제 환불 (refundPayment)
  - 환불 가능 상태 확인
  - PG사 환불 API 호출

**PG사 연동:**
- 현재는 시뮬레이션 모드
- 실제 구현 시 Stripe, PayPal, TossPayments 등 연동 가능

**의존성:**
- PaymentRepository
- OrderRepository

#### Review Service

**파일:** `xlcfi-review-service/src/main/java/com/xlcfi/review/service/ReviewService.java`

**주요 기능:**
- ✅ 리뷰 작성 (createReview)
  - 사용자/상품 검증
  - 중복 리뷰 확인
  - 구매 인증 여부 확인 (TODO)

- ✅ 리뷰 수정 (updateReview)
  - 작성자 권한 확인

- ✅ 리뷰 삭제 (deleteReview)
  - 소프트 삭제 (상태를 DELETED로 변경)

- ✅ 리뷰 조회 (getReview)
- ✅ 리뷰 목록 조회
  - 상품별 (getReviewsByProduct)
  - 사용자별 (getReviewsByUser)
  - 평점별 (getReviewsByRating)
  - 인증 구매 리뷰 (getVerifiedPurchaseReviews)
  - 최신 리뷰 (getLatestReviews)

**의존성:**
- ReviewRepository
- UserRepository
- ProductRepository

### 3. Security 설정

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/config/SecurityConfig.java`

**설정 내용:**
- Spring Security 활성화
- Stateless 세션 정책 (JWT 사용)
- CSRF 비활성화
- 공개 엔드포인트 설정:
  - `/api/auth/register` - 회원가입
  - `/api/auth/login` - 로그인
  - `/api/auth/refresh` - 토큰 갱신
- 나머지 엔드포인트는 인증 필요

**PasswordEncoder:** BCryptPasswordEncoder 사용

### 4. 예외 처리

모든 Service에서 일관된 예외 처리:

**BusinessException 사용:**
```java
throw new BusinessException("ERROR_CODE", "에러 메시지");
```

**주요 에러 코드:**

| 서비스 | 에러 코드 | 설명 |
|--------|----------|------|
| Auth | AUTH001 | 이미 존재하는 이메일 |
| Auth | AUTH002 | 이메일 또는 비밀번호 오류 |
| Auth | AUTH003 | 정지된 계정 |
| Auth | AUTH004 | 비활성화된 계정 |
| Auth | AUTH005 | 사용자를 찾을 수 없음 |
| Auth | AUTH006 | 유효하지 않은 리프레시 토큰 |
| Product | PRODUCT001 | 판매자를 찾을 수 없음 |
| Product | PRODUCT002 | 카테고리를 찾을 수 없음 |
| Product | PRODUCT003 | 상품을 찾을 수 없음 |
| Product | PRODUCT004-006 | 권한 없음 |
| Order | ORDER001 | 구매자를 찾을 수 없음 |
| Order | ORDER002 | 판매자를 찾을 수 없음 |
| Order | ORDER003 | 상품을 찾을 수 없음 |
| Order | ORDER004 | 재고 부족 |
| Order | ORDER005 | 주문을 찾을 수 없음 |
| Order | ORDER006-008 | 권한 없음 |
| Order | ORDER009 | 취소 불가 상태 |
| Order | ORDER010 | 잘못된 상태 전환 |
| Payment | PAYMENT001 | 주문을 찾을 수 없음 |
| Payment | PAYMENT002 | 결제 금액 불일치 |
| Payment | PAYMENT003 | 결제를 찾을 수 없음 |
| Payment | PAYMENT004 | 이미 처리된 결제 |
| Payment | PAYMENT005 | 결제 처리 오류 |
| Payment | PAYMENT006 | 환불 불가 상태 |
| Payment | PAYMENT007-008 | 환불 처리 오류 |
| Review | REVIEW001 | 사용자를 찾을 수 없음 |
| Review | REVIEW002 | 상품을 찾을 수 없음 |
| Review | REVIEW003 | 중복 리뷰 |
| Review | REVIEW004 | 리뷰를 찾을 수 없음 |
| Review | REVIEW005-006 | 권한 없음 |

## 트랜잭션 관리

- `@Transactional(readOnly = true)`: 클래스 레벨에 적용
- `@Transactional`: 쓰기 작업 메서드에 적용
- Spring의 선언적 트랜잭션 관리 활용

**예시:**
```java
@Service
@Transactional(readOnly = true)  // 기본값
public class ProductService {
    
    @Transactional  // 쓰기 작업
    public ProductResponse createProduct(...) {
        // ...
    }
    
    public ProductResponse getProduct(...) {  // 읽기 전용
        // ...
    }
}
```

## 로깅

- SLF4J + Logback 사용
- 주요 비즈니스 로직에 로그 추가
- 로그 레벨:
  - INFO: 주요 비즈니스 이벤트
  - WARN: 경고 (결제 실패 등)
  - ERROR: 에러 (예외 발생)

**예시:**
```java
log.info("상품 등록: sellerId={}, productName={}", sellerId, request.getName());
log.warn("결제 실패: paymentId={}", paymentId);
log.error("환불 처리 중 오류 발생: paymentId={}", paymentId, e);
```

## 의존성 주입

- Constructor Injection 사용 (권장 방식)
- Lombok의 `@RequiredArgsConstructor` 활용

**예시:**
```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
}
```

## 페이징 처리

- Spring Data JPA의 `Pageable` 인터페이스 활용
- `Page<Entity>` → `Page<DTO>` 변환

**예시:**
```java
public Page<ProductResponse> getProducts(Pageable pageable) {
    Page<Product> products = productRepository.findByStatus(
        ProductStatus.PUBLISHED, pageable);
    return products.map(ProductResponse::from);
}
```

## 향후 개선 사항

### 1. 인증/인가
- [ ] JWT 인증 필터 구현
- [ ] Role 기반 권한 검증 (@PreAuthorize)
- [ ] API Gateway에서 중앙 인증 처리

### 2. 캐싱
- [ ] Redis를 활용한 캐싱
  - 상품 정보
  - 카테고리 정보
  - 사용자 세션
- [ ] Spring Cache 추상화 활용

### 3. 비동기 처리
- [ ] 이메일 발송 (회원가입, 주문 확인)
- [ ] 알림 처리
- [ ] 이벤트 기반 아키텍처 (Spring Events 또는 Kafka)

### 4. 검증 강화
- [ ] 구매 인증 여부 실제 확인 (Review)
- [ ] 재고 동시성 제어
- [ ] 결제 금액 검증 강화

### 5. PG사 연동
- [ ] Stripe API 연동
- [ ] PayPal API 연동
- [ ] TossPayments API 연동
- [ ] 웹훅 처리

### 6. 테스트
- [ ] Unit Test 작성
- [ ] Integration Test 작성
- [ ] MockMvc를 활용한 Controller Test

### 7. 성능 최적화
- [ ] N+1 문제 해결 (fetch join)
- [ ] 쿼리 최적화
- [ ] 인덱스 추가

## 다음 단계

Service Layer 구현이 완료되었습니다. 다음 작업으로 진행할 수 있습니다:

1. **Controller Layer 구현** (추천)
   - REST API 엔드포인트 작성
   - Request Validation
   - Error Handling

2. **Security Filter 구현**
   - JWT 인증 필터
   - Role 기반 권한 검사

3. **API 문서화**
   - Swagger/OpenAPI 설정
   - API 명세 자동 생성

4. **테스트 코드 작성**
   - Service Layer Unit Test
   - Integration Test

5. **예외 처리 강화**
   - Global Exception Handler
   - 표준화된 Error Response

## 참고 문서

- [Database Schema Summary](./DATABASE_SCHEMA_SUMMARY.md): 데이터베이스 스키마 구현 내역
- [Java API Specs](../004.architecture/11_java_api_specs_detailed.md): Java API 상세 명세
- [Hybrid Architecture Design](../004.architecture/10_hybrid_architecture_design.md): 하이브리드 아키텍처 설계

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

